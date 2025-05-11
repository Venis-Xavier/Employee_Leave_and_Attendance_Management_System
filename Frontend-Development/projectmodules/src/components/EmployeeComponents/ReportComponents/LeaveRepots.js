import React, { useState } from "react";
import "./LeaveReports.css";

const LeaveReports = () => {
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [leaveType, setLeaveType] = useState("");
  const [leaveData, setLeaveData] = useState([]);
  const [loading, setLoading] = useState(false);

  // ✅ Function to parse token from localStorage
  const getParsedToken = () => {
    const tokenObject = localStorage.getItem("jwtToken");
    if (!tokenObject) return null;

    try {
      const parsedToken = JSON.parse(tokenObject);
      console.log("Parsed Token:", parsedToken); // ✅ Debugging log
      return parsedToken.data; // ✅ Extract only the actual JWT token
    } catch (error) {
      console.error("Error parsing JWT token:", error);
      return null;
    }
  };

  // ✅ Extract Employee ID from localStorage
  const employeeId = (() => {
    const user = JSON.parse(localStorage.getItem("userLoggedIn"));
    if (user && user.employeeId) {
      return user.employeeId;
    } else {
      alert("Employee ID not found. Please log in again.");
      throw new Error("Employee ID not found in localStorage.");
    }
  })();

  const handleSubmit = async () => {
    if (!startDate || !endDate) {
      alert("Please select both Start Date and End Date.");
      return;
    }

    try {
      setLoading(true);
      setLeaveData([]);

      const token = getParsedToken(); // ✅ Get parsed token
      if (!token) {
        alert("Missing authentication token. Please log in.");
        throw new Error("Authentication token not found.");
      }

      const response = await fetch(
        `http://localhost:8099/employee/${employeeId}/reports/leave?startDate=${startDate}&endDate=${endDate}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`, // ✅ Correct token format
          },
        }
      );

      if (!response.ok) {
        const errorMessage = await response.text();
        alert(`Error: ${errorMessage}`);
        return;
      }

      const data = await response.json();
      console.log("Leave Reports:", data); // ✅ Debugging log

      const filteredData =
        leaveType === "ALL_LEAVES"
          ? data
          : data.filter((leave) => leave.leaveType === leaveType);

      setLeaveData(filteredData);
    } catch (error) {
      console.error("Error fetching leave reports:", error);
      alert("An error occurred while fetching leave reports. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="leave-container">
      <h1>Leave Reports</h1>
      <div className="filter-container">
        <label>Start Date:</label>
        <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />

        <label>End Date:</label>
        <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />

        <label>Leave Type:</label>
        <select value={leaveType} onChange={(e) => setLeaveType(e.target.value)}>
          <option value="">Select Leave Type</option>
          <option value="SICK_LEAVE">Sick Leave</option>
          <option value="CASUAL_LEAVE">Casual Leave</option>
          <option value="PAID_LEAVE">Paid Leave</option>
          <option value="ALL_LEAVES">All Leaves</option>
        </select>

        <button onClick={handleSubmit}>Submit</button>
      </div>

      {loading && <p>Loading...</p>}

      {leaveData.length > 0 ? (
        <table className="leave-table">
          <thead>
            <tr>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Leave Type</th>
              <th>Leave Status</th>
            </tr>
          </thead>
          <tbody>
            {leaveData.map((leave, index) => (
              <tr key={index}>
                <td>{leave.startDate}</td>
                <td>{leave.endDate}</td>
                <td>{leave.leaveType}</td>
                <td>{leave.leaveStatus}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        !loading && <p>No data found</p>
      )}
    </div>
  );
};

export default LeaveReports;
