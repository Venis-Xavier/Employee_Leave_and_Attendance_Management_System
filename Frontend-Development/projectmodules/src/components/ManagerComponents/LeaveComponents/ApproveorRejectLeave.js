import React, { useState, useEffect, useCallback } from "react";
import { jwtDecode } from "jwt-decode";
import "./ApproveorRejectLeave.css";

const ApproveRejectLeave = () => {
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // ✅ Retrieve managerId from JWT token
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

  // ✅ Fetch pending leave requests dynamically
  const fetchLeaveRequests = useCallback(async () => {
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

      const response = await fetch(`http://localhost:8091/leaveRequest/getAllLeaveRequest/managerId/${managerId}`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Accept": "application/json",
        },
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Error fetching requests: ${response.status}`);
      }

      const responseBody = await response.text();
      const result = responseBody ? JSON.parse(responseBody) : {};

      if (result.success && Array.isArray(result.data)) {
        const pendingRequests = result.data.filter((req) => req.leaveStatus === "PENDING");
        setLeaveRequests(pendingRequests);
      } else {
        setLeaveRequests([]);
      }
    } catch (error) {
      console.error("Network/Server Error:", error);
      setError("Failed to fetch leave requests.");
    } finally {
      setLoading(false);
    }
  }, [managerId]);

  // ✅ Refresh requests on initial load
  useEffect(() => {
    fetchLeaveRequests();
  }, [fetchLeaveRequests]);

  // ✅ Handle Approve & Reject Actions
  const handleApprovalAction = async (employeeId, newStatus) => {
    setError(null);

    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) {
        setError("No JWT token found. Please log in.");
        return;
      }

      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;

      const response = await fetch(`http://localhost:8091/leaveRequest/leaveApprovalOrReject/employeeId/${employeeId}`, {
        method: "PATCH",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ leaveStatus: newStatus }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Error updating leave request: ${response.status}`);
      }

      const responseBody = await response.text();
      const result = responseBody ? JSON.parse(responseBody) : {};

      if (response.ok && result.success) {
        // ✅ Update pending list dynamically
        setLeaveRequests((prevRequests) => prevRequests.filter((req) => req.employeeId !== employeeId));
        fetchLeaveRequests(); // ✅ Refresh data from backend
      } else {
        setError(result.message || "Failed to update leave request.");
      }
    } catch (error) {
      console.error("Network/Server Error:", error);
      setError("Error updating leave request.");
    }
  };

  return (
    <div className="approve-reject-container">
      <h2>Pending Leave Requests</h2>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <p>Loading leave requests...</p>
      ) : (
        <table className="leave-table">
          <thead>
            <tr>
              <th>Employee ID</th>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Leave Type</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {leaveRequests.length > 0 ? (
              leaveRequests.map((request) => (
                <tr key={request.employeeId}>
                  <td>{request.employeeId}</td>
                  <td>{request.startDate}</td>
                  <td>{request.endDate}</td>
                  <td>{request.leaveType}</td>
                  <td>{request.leaveStatus}</td>
                  <td>
                    <button className="approve-btn" onClick={() => handleApprovalAction(request.employeeId, "APPROVED")}>
                      Approve
                    </button>
                    <button className="reject-btn" onClick={() => handleApprovalAction(request.employeeId, "REJECTED")}>
                      Reject
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="6" style={{ textAlign: "center", fontWeight: "bold" }}>
                  No pending leave requests found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default ApproveRejectLeave;
