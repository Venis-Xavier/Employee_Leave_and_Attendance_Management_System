import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import "./AssignShifts.css";

//  Retrieve token ONCE at the start and store it globally
const tokenObject = localStorage.getItem("jwtToken");
const parsedToken = tokenObject ? JSON.parse(tokenObject) : null;
const token = parsedToken ? parsedToken.data : null;

const AssignShifts = ({ setAssignments }) => {
  const [employeeId, setEmployeeId] = useState("");
  const [shiftName, setShiftName] = useState("DAYSHIFT");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [employees, setEmployees] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  //  Extract managerId from JWT token
  const getManagerIdFromToken = () => {
    try {
      if (!token) throw new Error("No JWT token found. Please log in.");
      const decodedToken = jwtDecode(token);
      return decodedToken?.employeeId || null;
    } catch (error) {
      console.error("Error decoding JWT token:", error);
      return null;
    }
  };

  const managerId = getManagerIdFromToken();
  console.log("Manager ID Used in API Fetch:", managerId);

  //  Fetch employees dynamically from backend
  useEffect(() => {
    if (!managerId) {
      console.error("Manager ID is undefined, skipping API call.");
      return;
    }

    const fetchEmployees = async () => {
      setError(null);

      try {
        if (!token) throw new Error("No JWT token found. Please log in.");

        const apiUrl = `http://localhost:8092/employee/employeesUnderManager/managerId/${managerId}`;
        console.log("Fetching Employees from:", apiUrl); //  Debugging

        const response = await fetch(apiUrl, {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Accept": "application/json",
          },
        });

        if (!response.ok) {
          const textResponse = await response.text();
          const errorData = textResponse ? JSON.parse(textResponse) : null;
          throw new Error(`Error fetching employees: ${response.status} - ${errorData?.message || response.statusText}`);
        }

        const textResponse = await response.text();
        if (!textResponse) throw new Error("Empty response received from the server.");

        const result = JSON.parse(textResponse);
        console.log("Raw Employee Data from API:", result);

        const employeeList = Array.isArray(result) ? result : result.data || [];
        console.log("Processed Employee List:", employeeList);

        setEmployees(employeeList);
      } catch (error) {
        console.error("Error fetching employees:", error);
        setError("Failed to fetch employees.");
      }
    };

    fetchEmployees();
  }, [managerId]);

  // Assign shift dynamically
  const handleAssignShift = async () => {
    if (!employeeId || !startDate || !endDate || !startTime || !endTime) {
      alert("Please select an employee and fill all fields.");
      return;
    }

    const formattedStartTime = `${startTime}:00`;
    const formattedEndTime = `${endTime}:00`;

    const newAssignment = {
      employeeId,
      shiftName,
      startDate,
      endDate,
      startTime: formattedStartTime,
      endTime: formattedEndTime,
    };

    setLoading(true);
    setError(null);

    try {
      if (!token) throw new Error("No JWT token found. Please log in.");

      const apiUrl = `http://localhost:8762/shiftassignments/assign/${employeeId}`;
      console.log("Assigning Shift to:", apiUrl);
      console.log("Payload Sent:", JSON.stringify(newAssignment));

      const response = await fetch(apiUrl, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(newAssignment),
      });

      const textResponse = await response.text();
      if (!textResponse) throw new Error("Empty response received from the server.");

      const responseBody = JSON.parse(textResponse);
      console.log("Shift assignment response:", responseBody);

      if (!response.ok || !responseBody.success) {
        const errorMessage = responseBody.message || `Error assigning shift: ${response.status}`;
        throw new Error(errorMessage);
      }

      setAssignments((prev) => [...prev, responseBody.data]);
      alert("Shift assigned successfully!");
    } catch (error) {
      console.error("Network/Server Error:", error);
      setError(error.message || "Error assigning shift.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="assign-shift-container">
      <h2>Assign Shift to Employee</h2>

      {error && <div className="error-message">{error}</div>}
      {loading && <p>Assigning shift...</p>}

      <div className="input-group">
        <label>Select Employee ID:</label>
        <select value={employeeId} onChange={(e) => setEmployeeId(e.target.value)}>
          <option value="">Select</option>
          {employees.length > 0 ? (
            employees.map((id) => (
              <option key={id} value={id}>
                {id}
              </option>
            ))
          ) : (
            <option disabled>No employees found</option>
          )}
        </select>
      </div>

      <div className="input-group">
        <label>Select Shift:</label>
        <select value={shiftName} onChange={(e) => setShiftName(e.target.value)}>
          <option value="DAYSHIFT">DAYSHIFT</option>
          <option value="NIGHTSHIFT">NIGHTSHIFT</option>
        </select>
      </div>

      <div className="input-group">
        <label>Start Date:</label>
        <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
      </div>

      <div className="input-group">
        <label>End Date:</label>
        <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
      </div>

      <div className="input-group">
        <label>Start Time:</label>
        <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} />
      </div>

      <div className="input-group">
        <label>End Time:</label>
        <input type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} />
      </div>

      <button className="assign-btn" onClick={handleAssignShift} disabled={loading}>
        Assign Shift
      </button>
    </div>
  );
};

export default AssignShifts;