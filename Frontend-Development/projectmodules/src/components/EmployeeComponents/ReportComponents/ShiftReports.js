import React, { useState } from "react";
import { jwtDecode } from "jwt-decode";
import { format } from "date-fns"; // Import date-fns for date formatting
import "./ShiftReports.css";

const ShiftReports = () => {
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [shiftData, setShiftData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

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
      return parsedToken.data; // ✅ Extract only the actual JWT token
    } catch (error) {
      console.error("Error parsing JWT token:", error);
      return null;
    }
  };

  const fetchShiftData = async () => {
    if (!startDate || !endDate) {
      alert("Please select both Start Date and End Date.");
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const token = getParsedToken(); // ✅ Get parsed token
      if (!token) {
        alert("Missing authentication token. Please log in.");
        throw new Error("Authentication token not found.");
      }

      const decodedToken = jwtDecode(token);
      const employeeId = decodedToken?.employeeId || null; // ✅ Extract employee ID safely

      if (!employeeId) {
        alert("Invalid token: Employee ID not found.");
        throw new Error("Employee ID missing in JWT payload.");
      }

      console.log("Extracted Employee ID:", employeeId);

      // ✅ API call with startDate & endDate using fetch
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
      console.log("Raw Shift Data Response:", data);

      // ✅ Ensure API response format is an array before formatting
      const shiftRecords = Array.isArray(data)
        ? data.map((record) => ({
            ...record,
            startDate: format(new Date(record.startDate), "dd-MM-yyyy"), // Format startDate
            endDate: format(new Date(record.endDate), "dd-MM-yyyy"), // Format endDate
          }))
        : [];

      console.log("Formatted Shift Data:", shiftRecords);
      setShiftData(shiftRecords);
    } catch (err) {
      console.error("Error fetching shift data:", err);
      setError("An error occurred while fetching shift data. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="shift-container">
      <h1>My Shift Reports</h1>
      <div className="filter-container">
        <div>
          <label>Start Date:</label>
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div>
          <label>End Date:</label>
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
        <button onClick={fetchShiftData}>Fetch Shifts</button>
      </div>

      {loading && <p>Loading...</p>}
      {error && <p className="error">{error}</p>}

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
            {shiftData.map((record, index) => (
              <tr key={index}>
                <td>{record.startDate || "N/A"}</td>
                <td>{record.startTime || "N/A"}</td>
                <td>{record.endTime || "N/A"}</td>
                <td>{record.shiftName || "N/A"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        !loading && <p>No shifts found for the selected date range.</p>
      )}
    </div>
  );
};

export default ShiftReports;
