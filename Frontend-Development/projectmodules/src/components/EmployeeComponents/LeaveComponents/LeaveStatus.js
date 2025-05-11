import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import "bootstrap/dist/css/bootstrap.min.css";

const LeaveStatus = () => {
  const { employeeId: urlEmployeeId } = useParams();
  const navigate = useNavigate();
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  // ✅ Retrieve employeeId from JWT token
  const getEmployeeIdFromToken = () => {
    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) return null;

      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;
      const decodedToken = jwtDecode(token);
      return decodedToken?.employeeId || null;
    } catch (error) {
      console.error("Error decoding JWT token:", error);
      return null;
    }
  };

  const employeeId = getEmployeeIdFromToken() || urlEmployeeId;

  useEffect(() => {
    if (!employeeId || isNaN(Number(employeeId))) {
      setError("Invalid Employee ID. Please try again.");
      setLoading(false);
      return;
    }

    const fetchLeaveRequests = async () => {
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
        const token = parsedToken.data;

        const response = await fetch(`http://localhost:8091/leaveRequest/showLeaveRequest/employeeId/${employeeId}`, {
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

        const result = await response.json();

        if (result) {
          setLeaveRequests(result.data);
        } else {
          setLeaveRequests([]);
        }
      } catch (error) {
        console.error("Network/Server Error:", error);
        setError("Failed to fetch leave requests. Please check your connection.");
        setLeaveRequests([]);
      } finally {
        setLoading(false);
      }
    };

    fetchLeaveRequests();
  }, [employeeId]);

  // ✅ Handle leave cancellation
  const handleCancelLeave = async (leaveRequestId) => {
    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) {
        alert("No JWT token found. Please log in.");
        return;
      }

      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;

      const response = await fetch(
        `http://localhost:8091/leaveRequest/cancelLeaveRequest/employeeId/${employeeId}/leaveRequestId/${leaveRequestId}`,
        {
          method: "DELETE",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        throw new Error(`Error canceling leave: ${response.status}`);
      }

      const result = await response.json();

      if (result.success) {
        setLeaveRequests((prevRequests) =>
          prevRequests.filter((req) => req.leaveRequestId !== leaveRequestId)
        );
        alert(result.message || "Leave request canceled successfully.");
      } else {
        alert(result.message || "Failed to cancel leave request.");
      }
    } catch (error) {
      console.error("Error canceling leave request:", error);
      alert("Failed to cancel leave request. Please try again.");
    }
  };

  return (
    <div className="container leave-status-container" style={{ marginTop: '20px' }}> {/* Added marginTop */}
      <h2 className="text-center">Leave Status</h2>

      {error && <div className="alert alert-danger text-center">{error}</div>}

      {loading ? (
        <div className="text-center">
          <p className="spinner-border text-primary"></p>
          <p>Loading leave requests...</p>
        </div>
      ) : (
        <div className="d-flex justify-content-center">
          <div className="table-responsive" style={{ width: '95%' }}>
            <table className="table table-striped table-bordered leave-status-table" style={{ fontSize: '1.1rem' }}>
              <thead className="table-dark">
                <tr>
                  <th style={{ width: '15%' }}>Leave Request ID</th>
                  <th style={{ width: '20%' }}>Leave Type</th>
                  <th style={{ width: '15%' }}>Start Date</th>
                  <th style={{ width: '15%' }}>End Date</th>
                  <th style={{ width: '15%' }}>Status</th>
                  <th style={{ width: '20%' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {leaveRequests.length > 0 ? (
                  leaveRequests.map((req, index) => (
                    <tr key={index} style={{ fontSize: '1rem' }}>
                      <td>{req.leaveRequestId || "N/A"}</td>
                      <td>{req.leaveType?.replace("_", " ") || "N/A"}</td>
                      <td>{req.startDate || "N/A"}</td>
                      <td>{req.endDate || "N/A"}</td>
                      <td>
                        <span className={`badge ${req.leaveStatus === "APPROVED" ? "bg-success" : req.leaveStatus === "REJECTED" ? "bg-danger" : req.leaveStatus === "PENDING" ? "bg-warning text-dark" : "bg-secondary"}`}>
                          {req.leaveStatus || "Unknown"}
                        </span>
                      </td>
                      <td>
                        {req.leaveStatus === "PENDING" && (
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => handleCancelLeave(req.leaveRequestId)}
                          >
                            Cancel
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" className="text-center fw-bold" style={{ fontSize: '1.1rem' }}>
                      No leave requests available.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div className="text-center mt-3">
        <button className="btn back-button" onClick={() => navigate(`/leaves/${employeeId}`)}>
          ← Back to Leave Dashboard
        </button>
      </div>
    </div>
  );
};

export default LeaveStatus;