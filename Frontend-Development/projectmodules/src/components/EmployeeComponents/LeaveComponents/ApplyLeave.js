import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import "bootstrap/dist/css/bootstrap.min.css";

const ApplyLeave = () => {
  const navigate = useNavigate();

  const [leaveType, setLeaveType] = useState("SICK_LEAVE");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

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

  const loggedInEmployeeId = getEmployeeIdFromToken();

  // ✅ Load existing leave requests from localStorage
  useEffect(() => {
    try {
      const stored = localStorage.getItem("leaveRequests");
      if (stored) {
        const parsed = JSON.parse(stored);
        if (Array.isArray(parsed)) {
          setLeaveRequests(parsed);
        }
      }
    } catch (err) {
      console.error("Error loading from localStorage:", err);
    }
  }, []);

  // ✅ Save to localStorage whenever leaveRequests changes
  useEffect(() => {
    localStorage.setItem("leaveRequests", JSON.stringify(leaveRequests));
  }, [leaveRequests]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setMessage(null);

    const tokenObject = localStorage.getItem("jwtToken");

    if (!tokenObject || !loggedInEmployeeId) {
      setError("You must be logged in to apply for leave.");
      return;
    }

    const parsedToken = JSON.parse(tokenObject);
    const token = parsedToken.data;

    const newRequest = {
      employeeId: loggedInEmployeeId,
      leaveType,
      startDate,
      endDate,
    };

    try {
      const response = await fetch("http://localhost:8091/leaveRequest/sendLeaveRequest", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(newRequest),
      });

      const responseText = await response.text();
      const result = responseText ? JSON.parse(responseText) : {};

      console.log("Leave Request API Response:", result);

      if (!response.ok || !result.success) {
        const errorMessage = result.message || `Failed to apply for leave: ${response.status}`;
        throw new Error(errorMessage);
      }

      setMessage(result.message || "Leave request sent successfully!");
      setLeaveRequests((prev) => [...prev, { ...newRequest, status: "Pending" }]);
      setLeaveType("SICK_LEAVE");
      setStartDate("");
      setEndDate("");
    } catch (error) {
      console.error("Network error submitting leave request:", error);
      setError(error.message || "Could not apply for leave. Please check your connection.");
    }
  };

  return (
    <div className="container mt-5">
  <div className="card shadow-lg p-4 mx-auto" style={{ maxWidth: "650px", width: "100%" }}>
    <h2 className="text-center mb-4 text-primary">Leave Request Form</h2>

        {error && <div className="alert alert-danger">{error}</div>}
        {message && <div className="alert alert-success">{message}</div>}

        <form onSubmit={handleSubmit}>
          <div className="mb-3 row align-items-center">
            <div className="col-md-3">
              <label htmlFor="leaveType" className="form-label">Leave Type:</label>
            </div>
            <div className="col-md-9">
              <select
                className="form-select form-select-sm w-100"
                id="leaveType"
                value={leaveType}
                onChange={(e) => setLeaveType(e.target.value)}
                style={{
                  width: "100px", /* ✅ Matches date pickers */
                  height: "38px", /* ✅ Matches date pickers */
                  fontSize: "14px",
                  padding: "6px"
                }}
              >
                <option value="SICK_LEAVE">Sick Leave</option>
                <option value="CASUAL_LEAVE">Casual Leave</option>
                <option value="PAID_LEAVE">Paid Leave</option>
              </select>
            </div>
          </div>

          <div className="mb-3 row align-items-center">
            <div className="col-md-3">
              <label htmlFor="startDate" className="form-label">Start Date:</label>
            </div>
            <div className="col-md-9">
              <input
                type="date"
                className="form-control form-control-sm w-100"
                id="startDate"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
                style={{
                  width: "100px", /* ✅ Matches dropdown */
                  height: "38px", /* ✅ Matches dropdown */
                  fontSize: "14px",
                  padding: "6px"
                }}
              />
            </div>
          </div>

          <div className="mb-3 row align-items-center">
            <div className="col-md-3">
              <label htmlFor="endDate" className="form-label">End Date:</label>
            </div>
            <div className="col-md-9">
              <input
                type="date"
                className="form-control form-control-sm w-100"
                id="endDate"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                required
                style={{
                  width: "100px", /* ✅ Matches dropdown */
                  height: "38px", /* ✅ Matches dropdown */
                  fontSize: "14px",
                  padding: "6px"
                }}
              />
            </div>
          </div>

          <div className="d-flex justify-content-center">
            <button type="submit" className="btn btn-primary btn-sm">
              Submit Leave Request
            </button>
          </div>
        </form>

        {leaveRequests.length > 0 && (
          <div className="mt-4">
            <h3>Leave Requests</h3>
            <div className="table-responsive">
              <table className="table table-striped table-bordered table-sm">
                <thead>
                  <tr>
                    <th>Leave Type</th>
                    <th>Start Date</th>
                    <th>End Date</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {leaveRequests.map((req, index) => (
                    <tr key={index}>
                      <td>{req.leaveType}</td>
                      <td>{req.startDate}</td>
                      <td>{req.endDate}</td>
                      <td>
                        <span className={`badge bg-${req.status?.toLowerCase() === 'pending' ? 'warning' : req.status?.toLowerCase() === 'approved' ? 'success' : req.status?.toLowerCase() === 'rejected' ? 'danger' : 'secondary'}`}>
                          {req.status || "Unknown"}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ApplyLeave;
