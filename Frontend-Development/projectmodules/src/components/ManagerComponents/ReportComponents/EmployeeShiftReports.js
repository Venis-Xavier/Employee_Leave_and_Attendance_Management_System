import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import "./EmployeeShiftReports.css";

// ✅ Function to parse and extract token from localStorage
const getParsedToken = () => {
  const tokenObject = localStorage.getItem("jwtToken");
  if (!tokenObject) {
    console.error("JWT token not found in localStorage.");
    return null;
  }

  try {
    const parsedToken = JSON.parse(tokenObject);
    console.log("Parsed Token Data:", parsedToken); // ✅ Debugging log
    return parsedToken.data; // ✅ Extract actual JWT token
  } catch (error) {
    console.error("Error parsing JWT token:", error);
    return null;
  }
};

const EmployeeShiftReports = () => {
  const [employeeId, setEmployeeId] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [shiftType, setShiftType] = useState("");
  const [shiftData, setShiftData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [employees, setEmployees] = useState([]);

  // ✅ Fetch employee IDs and names from the backend
  useEffect(() => {
    const fetchEmployees = async () => {
      try {
        const token = getParsedToken(); // ✅ Get parsed token
        if (!token) {
          alert("Missing authentication token. Please log in.");
          throw new Error("Authentication token not found.");
        }

        const decodedToken = jwtDecode(token);
        const managerId = decodedToken?.employeeId || null; // ✅ Extract Manager ID safely

        if (!managerId) {
          alert("Invalid token: Manager ID not found.");
          throw new Error("Manager ID missing in JWT payload.");
        }

        console.log("Extracted Manager ID:", managerId);

        const response = await fetch(`http://localhost:8099/employee/${managerId}/reports/getAllNamesAndIds`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`, // ✅ Correct token format
          },
        });

        if (!response.ok) {
          throw new Error(`Error fetching employees: ${response.status}`);
        }

        const data = await response.json();
        console.log("Employees Under Manager Response:", data);

// Convert the object into an array of key-value pairs
const employeeList = Object.entries(data).map(([id, name]) => ({
  id: parseInt(id, 10), // Convert the key to an integer
  name,
}));


        setEmployees(employeeList);
      } catch (error) {
        console.error("Error fetching employee data:", error);
        alert("Failed to load employee data. Please try again later.");
      }
    };

    fetchEmployees();
  }, []);

  const handleSubmit = async () => {
    if (!employeeId || !startDate || !endDate || !shiftType) {
      alert("Please fill out all fields before submitting.");
      return;
    }

    try {
      setLoading(true);
      const token = getParsedToken(); // ✅ Get parsed token
      if (!token) {
        alert("Missing authentication token. Please log in.");
        throw new Error("Authentication token not found.");
      }

      const url = `http://localhost:8099/employee/${employeeId}/reports/shift?startDate=${startDate}&endDate=${endDate}`;

      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`, // ✅ Correct token format
        },
      });

      if (!response.ok) {
        throw new Error(`Error fetching shift data: ${response.status}`);
      }

      const data = await response.json();
      console.log("Raw Shift Data:", data);

      const filteredData =
        shiftType === "ALL_SHIFTS"
          ? data
          : data.filter((shift) => shift.shiftName === shiftType);

      console.log("Filtered Shift Data:", filteredData);
      setShiftData(filteredData);
    } catch (error) {
      console.error("Error fetching shift data:", error);
      alert("An error occurred while fetching shift data. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="shift-container">
      <h1>Employee Shift Reports</h1>
      <div className="filter-container">
        <div>
          <label>Employee:</label>
          <select value={employeeId} onChange={(e) => setEmployeeId(e.target.value)}>
            <option value="">Select Employee</option>
            {employees.map((employee) => (
              <option key={employee.id} value={employee.id}>
                {employee.name} ({employee.id})
              </option>
            ))}
          </select>
        </div>
        <div>
          <label>Start Date:</label>
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div>
          <label>End Date:</label>
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
        <div>
          <label>Shift Type:</label>
          <select value={shiftType} onChange={(e) => setShiftType(e.target.value)}>
            <option value="">Select Shift Type</option>
            <option value="ALL_SHIFTS">All Shifts</option>
            <option value="DAYSHIFT">Day Shift</option>
            <option value="NIGHTSHIFT">Night Shift</option>
          </select>
        </div>
        <button onClick={handleSubmit}>Submit</button>
      </div>

      {loading && <p>Loading...</p>}

      {shiftData.length > 0 ? (
        <table className="shift-table">
          <thead>
            <tr>
              <th>Shift Date</th>
              <th>Start Time</th>
              <th>End Time</th>
              <th>Shift Name</th>
            </tr>
          </thead>
          <tbody>
            {shiftData.flatMap((shift, index) => {
              const startDate = new Date(shift.startDate);
              const endDate = new Date(shift.endDate);
              const dates = [];

              for (let d = startDate; d <= endDate; d.setDate(d.getDate() + 1)) {
                dates.push(new Date(d));
              }

              return dates.map((date, dateIndex) => (
                <tr key={`${index}-${dateIndex}`}>
                  <td>{date.toISOString().split("T")[0]}</td>
                  <td>{shift.startTime}</td>
                  <td>{shift.endTime}</td>
                  <td>{shift.shiftName}</td>
                </tr>
              ));
            })}
          </tbody>
        </table>
      ) : (
        !loading && <p>No data found</p>
      )}
    </div>
  );
};

export default EmployeeShiftReports;
