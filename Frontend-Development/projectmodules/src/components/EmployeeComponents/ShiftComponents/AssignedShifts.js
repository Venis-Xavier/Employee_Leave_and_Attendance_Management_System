
import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import "bootstrap/dist/css/bootstrap.min.css"; //  Bootstrap included

const AssignedShifts = () => {
  const [assignments, setAssignments] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchAssignments(); //  Fetch assignments for the logged-in user
  }, []);

  //  Extract Employee ID from JWT token
  const getEmployeeIdFromToken = () => {
    const tokenObject = localStorage.getItem("jwtToken");
    if (!tokenObject) {
      setError("No JWT token found. Please log in.");
      return null;
    }

    try {
      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;
      const decodedToken = jwtDecode(token);
      return decodedToken?.employeeId;
    } catch (error) {
      console.error("Error decoding JWT token:", error);
      setError("Invalid JWT token.");
      return null;
    }
  };

  //  Fetch Assignment Data
  const fetchAssignments = async () => {
    setError(null);
    setAssignments([]);
    setLoading(true);

    const employeeIdFromToken = getEmployeeIdFromToken();
    if (!employeeIdFromToken) {
      setLoading(false);
      return;
    }

    const tokenObject = localStorage.getItem("jwtToken");
    const parsedToken = tokenObject ? JSON.parse(tokenObject) : null;
    const token = parsedToken ? parsedToken.data : null;

    if (!token) {
      setError("No valid authentication token found.");
      setLoading(false);
      return;
    }

    try {
      const apiUrl = `http://localhost:8762/shiftassignments/employee/${employeeIdFromToken}`;
      console.log("Fetching Assignments from:", apiUrl);

      const response = await fetch(apiUrl, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: "application/json",
        },
      });

      if (!response.ok) {
        const errorResponse = await response.text();
        throw new Error(errorResponse || `Error fetching assignments: ${response.status}`);
      }

      const result = await response.json();
      console.log("Assignments Response:", result);

      //  Ensure success response and correct data structure
      if (result && result.success && Array.isArray(result.data)) {
        setAssignments(result.data); //  Store all shift assignments in state
      } else {
        setError(result?.message || "No assignments found for your employee ID.");
      }
    } catch (error) {
      console.error("Error fetching assignment details:", error);
      setError(error.message || "Failed to fetch assignment details.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-5">
      <div className="card shadow-lg p-4" style={{ maxWidth: "900px", margin: "auto" }}>
        <h2 className="text-center text-primary">Your Shift Assignments</h2>

        {error && <p className="alert alert-danger fw-bold">{error}</p>}
        {loading && <p className="text-warning fw-bold">Fetching your assignment details...</p>}

        {assignments.length > 0 ? (
          <div className="mt-4">
            <h3 className="text-success fw-bold text-center">Assignment Details</h3>
            <div className="table-responsive">
              <table className="table table-bordered">
                <thead className="table-warning">
                  <tr>
                    <th>Assignment ID</th>
                    <th>Shift Name</th>
                    <th>Start Date</th>
                    <th>End Date</th>
                    <th>Start Time</th>
                    <th>End Time</th>
                  </tr>
                </thead>
                <tbody>
                  {assignments.map((assignment, index) => (
                    <tr key={index}>
                      <td>{assignment.shiftAssignmentId}</td>
                      <td>{assignment.shiftName}</td>
                      <td>{assignment.startDate}</td>
                      <td>{assignment.endDate}</td>
                      <td>{assignment.startTime}</td>
                      <td>{assignment.endTime}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        ) : (
          !loading && !error && <p className="text-muted fw-bold text-center">No assignment details found.</p>
        )}
      </div>
    </div>
  );
};

export default AssignedShifts;
