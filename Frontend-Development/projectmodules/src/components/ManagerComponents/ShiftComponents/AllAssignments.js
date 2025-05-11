import React, { useState, useEffect, useCallback } from "react";
import { jwtDecode } from "jwt-decode";
import "./AllAssignments.css";

const AllAssignments = () => {
  const [assignments, setAssignments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Retrieve managerId from JWT token
  const getManagerIdFromToken = useCallback(() => {
    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (tokenObject) {
        const parsedToken = JSON.parse(tokenObject); // Parse JSON object
        const token = parsedToken.data; // Extract JWT string
        const decodedToken = jwtDecode(token);
        return decodedToken?.employeeId || null;
      }
    } catch (error) {
      console.error("Error decoding JWT token:", error);
    }
    return null;
  }, []);

  const managerId = getManagerIdFromToken();

  // Fetch shift assignments dynamically
  const fetchShiftAssignments = useCallback(async () => {
    setLoading(true);
    setError(null);

    if (!managerId) {
      setError("Unauthorized access. Please log in.");
      setLoading(false);
      return;
    }

    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) {
        setError("No JWT token found. Please log in.");
        setLoading(false);
        return;
      }

      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;

      const response = await fetch(`http://localhost:8762/shiftassignments/getShiftAssignmentsOfEmployeesUnderManager/${managerId}`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Accept": "application/json",
        },
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Error fetching assignments: ${response.status}`);
      }

      const responseBody = await response.text();
      const result = responseBody ? JSON.parse(responseBody) : {};

      console.log("Fetched Data:", result); 
      console.log("Shift Assignments:", result.data);

      if (result.success && Array.isArray(result.data)) {
        setAssignments(result.data);
      } else {
        setAssignments([]);
      }
    } catch (error) {
      console.error("Network/Server Error:", error);
      setError("Failed to fetch shift assignments.");
    } finally {
      setLoading(false);
    }
  }, [managerId]);

  //Fetch assignments on initial load
  useEffect(() => {
    fetchShiftAssignments();
  }, [fetchShiftAssignments]);

  return (
    <div className="shift-assignments-container">
      <h2>Assigned Shifts</h2>

      {error && <div className="error-message">{error}</div>}
      {loading ? (
        <p>Loading shift assignments...</p>
      ) : (
        <table className="shift-assignments-table">
          <thead>
            <tr>
              <th>Employee ID</th>
              <th>Shift Name</th>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Start Time</th>
              <th>End Time</th>
            </tr>
          </thead>
          <tbody>
            {assignments.length > 0 ? (
              assignments.map((employee) => (
                <tr key={employee.employeeId}>
                  <td>{employee.employeeId}</td>
                  <td>{employee.shiftName}</td>
                  <td>{employee.startDate}</td>
                  <td>{employee.endDate}</td>
                  <td>{employee.startTime}</td>
                  <td>{employee.endTime}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="6" style={{ textAlign: "center", fontWeight: "bold" }}>
                  No shift assignments found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default AllAssignments;
