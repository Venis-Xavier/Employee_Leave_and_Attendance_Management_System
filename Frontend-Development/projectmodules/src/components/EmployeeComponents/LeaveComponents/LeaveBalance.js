import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import { useNavigate } from "react-router-dom";

const LeaveBalance = () => {
  const navigate = useNavigate();
  const [leaveBalances, setLeaveBalances] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  const publicHolidays = [
    { date: "2025-01-01", name: "New Year's Day" },
    { date: "2025-01-26", name: "Republic Day" },
    { date: "2025-08-15", name: "Independence Day" },
    { date: "2025-10-02", name: "Gandhi Jayanti" },
    { date: "2025-12-25", name: "Christmas" },
  ];

  // ✅ Retrieve employeeId from JWT token
  const getEmployeeIdFromToken = () => {
    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (tokenObject) {
        const parsedToken = JSON.parse(tokenObject);
        const token = parsedToken.data;
        const decodedToken = jwtDecode(token);
        return decodedToken?.employeeId || null;
      }
    } catch (error) {
      console.error("Error decoding JWT token:", error);
    }
    return null;
  };

  const employeeId = getEmployeeIdFromToken();

  useEffect(() => {
    const fetchLeaveBalance = async () => {
      setError(null);
      setLoading(true);

      if (!employeeId) {
        setError("Error: Employee ID not found in token.");
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

        const response = await fetch(`http://localhost:8091/leaveBalance/showLeaveBalance/employeeId/${employeeId}`, {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Accept": "application/json",
          },
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || `Server error: ${response.status} ${response.statusText}`);
        }

        const responseBody = await response.text();
        const result = responseBody ? JSON.parse(responseBody) : {};

        console.log("Leave Balance Data:", result.data);

        setLeaveBalances(Array.isArray(result.data) ? result.data.map(lb => ({
          leaveTypes: lb?.leaveTypes ?? "Unknown",
          balance: lb?.balance ?? "N/A"
        })) : []);
      } catch (error) {
        console.error("Error fetching leave balance:", error);
        setError("Failed to fetch leave balance. Please check your connection.");
      } finally {
        setLoading(false);
      }
    };

    fetchLeaveBalance();
  }, [employeeId]);

  return (
    <div style={{ padding: "30px", maxWidth: "800px", margin: "auto", backgroundColor: "#fff", borderRadius: "12px", boxShadow: "0 6px 15px rgba(0, 0, 0, 0.1)" }}>
      <h2 style={{ textAlign: "center", color: "#2f3e9e" }}>Leave Balance</h2>

      {error && <div style={{ color: "red", marginBottom: "15px" }}>{error}</div>}

      {loading ? (
        <p>Loading leave balances...</p>
      ) : leaveBalances.length > 0 ? (
        <div style={{ display: "flex", justifyContent: "space-around", margin: "20px 0", gap: "15px" }}> {/* ✅ Added spacing */}
          {leaveBalances.map((leave, idx) => (
            <div key={idx} style={{
              padding: "15px 25px",
              borderRadius: "10px",
              fontWeight: "bold",
              fontSize: "18px",
              textAlign: "center",
              color: "white",
              backgroundColor: leave.leaveTypes?.toLowerCase() === "sick_leave" ? "#e74c3c" :
                leave.leaveTypes?.toLowerCase() === "casual_leave" ? "#f39c12" :
                  leave.leaveTypes?.toLowerCase() === "paid_leave" ? "#2ecc71" : "#6c757d",
            }}>
              <strong>{leave.leaveTypes?.replace("_", " ") || "Unknown Leave Type"}:</strong>
              <span style={{ marginLeft: "10px", color: "white", fontWeight: "bold" }}>{leave.balance}</span>
            </div>
          ))}
        </div>
      ) : (
        <p>No leave balances available.</p>
      )}

      <h3 style={{ textAlign: "center", color: "#2f3e9e" }}>Public Holidays - 2025</h3>
      <div className="table-responsive">
        <table style={{ width: "100%", borderCollapse: "collapse", marginTop: "20px" }}>
          <thead>
            <tr style={{ border: "1px solid #ccc", padding: "10px", textAlign: "center" }}>
              <th>Date</th>
              <th>Holiday</th>
            </tr>
          </thead>
          <tbody>
            {publicHolidays.map((holiday, idx) => (
              <tr key={idx} style={{ fontSize: "0.9rem", border: "1px solid #ccc", padding: "10px", textAlign: "center" }}>
                <td>{holiday.date}</td>
                <td>{holiday.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <button style={{ marginTop: "30px", display: "block", padding: "10px 20px", backgroundColor: "#2f3e9e", color: "white", fontWeight: "bold", borderRadius: "8px", border: "none", cursor: "pointer", marginLeft: "auto", marginRight: "auto" }}
        onClick={() => navigate("/leaves")}>
        ← Back to Leave Dashboard
      </button>
    </div>
  );
};

export default LeaveBalance;
