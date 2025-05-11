import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import "./EmployeeLeaveBalance.css";

const LeaveBalance = () => {
  const { managerId: urlManagerId } = useParams();
  const [leaveBalances, setLeaveBalances] = useState([]);
  const [filteredBalances, setFilteredBalances] = useState([]);
  const [searchEmployeeId, setSearchEmployeeId] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  // ✅ Retrieve managerId from JWT token
  const getManagerIdFromToken = () => {
    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (tokenObject) {
        const parsedToken = JSON.parse(tokenObject); // Parse stored JSON object
        const token = parsedToken.data; // Extract JWT token string
        const decodedToken = jwtDecode(token);
        return decodedToken?.employeeId || null;
      }
    } catch (error) {
      console.error("Error decoding JWT token:", error);
    }
    return null;
  };

  const managerId = getManagerIdFromToken() || urlManagerId;

  useEffect(() => {
    console.log("Retrieved Manager ID:", managerId);

    if (!managerId || isNaN(Number(managerId))) {
      setError("Invalid Manager ID. Please try again.");
      setLoading(false);
      return;
    }

    const fetchLeaveBalances = async () => {
      setError(null);
      setLoading(true);

      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) {
        setError("No JWT token found. Please log in.");
        setLoading(false);
        return;
      }

      try {
        const parsedToken = JSON.parse(tokenObject);
        const token = parsedToken.data; // Extract token
        console.log(`Fetching leave balances for manager ID: ${managerId}`);

        const response = await fetch(`http://localhost:8091/leaveBalance/showLeaveBalanceOfAllEmployeesUnderManager/${managerId}`, {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Accept": "application/json",
          },
        });

        console.log("HTTP Response Status:", response.status);

        if (!response.ok) {
          throw new Error(`Network error: ${response.status} ${response.statusText}`);
        }

        const result = await response.json();
        console.log("API Response:", result);

        if (result.success && Array.isArray(result.data)) {
          const formattedBalances = result.data.map((leaveArray) => ({
            employeeId: String(leaveArray[0]?.employeeId || "N/A"),
            sickLeave: leaveArray.find((l) => l.leaveTypes === "SICK_LEAVE")?.balance || 0,
            paidLeave: leaveArray.find((l) => l.leaveTypes === "PAID_LEAVE")?.balance || 0,
            casualLeave: leaveArray.find((l) => l.leaveTypes === "CASUAL_LEAVE")?.balance || 0,
            totalBalance:
              (leaveArray.find((l) => l.leaveTypes === "SICK_LEAVE")?.balance || 0) +
              (leaveArray.find((l) => l.leaveTypes === "PAID_LEAVE")?.balance || 0) +
              (leaveArray.find((l) => l.leaveTypes === "CASUAL_LEAVE")?.balance || 0),
          }));

          setLeaveBalances(formattedBalances);
          setFilteredBalances(formattedBalances);
        } else {
          console.warn("No leave balances found or unexpected response format:", result);
          setLeaveBalances([]);
          setFilteredBalances([]);
        }
      } catch (error) {
        console.error("Network/Server Error:", error);
        setError("Failed to fetch leave balances. Please check your connection.");
        setLeaveBalances([]);
        setFilteredBalances([]);
      } finally {
        setLoading(false);
      }
    };

    fetchLeaveBalances();
  }, [managerId]);

  // ✅ Handle Employee ID Search Dynamically
  useEffect(() => {
    const searchId = searchEmployeeId.trim();
    setFilteredBalances(searchId ? leaveBalances.filter((leave) => leave.employeeId === searchId) : leaveBalances);
  }, [searchEmployeeId, leaveBalances]);

  return (
    <div className="leave-balance-container">
      <h2>Employee Leave Balance</h2>

      {error && <div className="error-message">{error}</div>}

      {/* ✅ Employee Search Box */}
      <div className="search-box">
        <input
          type="text"
          placeholder="Enter Employee ID"
          value={searchEmployeeId}
          onChange={(e) => setSearchEmployeeId(e.target.value)}
        />
      </div>

      {loading ? (
        <p>Loading leave balances...</p>
      ) : (
        <table className="leave-balance-table">
          <thead>
            <tr>
              <th>Employee ID</th>
              <th>Sick Leave</th>
              <th>Paid Leave</th>
              <th>Casual Leave</th>
              <th>Total Balance</th>
            </tr>
          </thead>
          <tbody>
            {filteredBalances.length > 0 ? (
              filteredBalances.map((leave, index) => (
                <tr key={index}>
                  <td>{leave.employeeId}</td>
                  <td>{leave.sickLeave}</td>
                  <td>{leave.paidLeave}</td>
                  <td>{leave.casualLeave}</td>
                  <td>{leave.totalBalance}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" style={{ textAlign: "center", fontWeight: "bold" }}>
                  No leave balances available.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default LeaveBalance;
 