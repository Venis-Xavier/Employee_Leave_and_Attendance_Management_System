import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import "bootstrap/dist/css/bootstrap.min.css"; // ✅ Bootstrap included

const ShiftRequest = () => {
  const navigate = useNavigate();
  const [pendingRequest, setPendingRequest] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [shiftName, setShiftName] = useState("DAYSHIFT");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");

  useEffect(() => {
    fetchPendingRequest();
  }, []);

  const formatTime = (time) => (time ? `${time}:00` : "");

  const fetchPendingRequest = async () => {
    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) {
        throw new Error("No JWT token found. Please log in.");
      }

      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;
      const decodedToken = jwtDecode(token);
      const employeeId = decodedToken?.employeeId;

      if (!employeeId) {
        throw new Error("Invalid Employee ID extracted from token.");
      }

      console.log(`Fetching pending shift request for Employee ID: ${employeeId}`);

      const response = await axios.get(`http://localhost:8762/shift-requests/employee/${employeeId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!response || !response.data || !response.data.success) {
        throw new Error(response?.data?.message || "Failed to fetch pending request: Unexpected error");
      }

      setPendingRequest(response.data.data);
    } catch (err) {
      console.error("Error fetching pending shift request:", err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) throw new Error("No JWT token found.");

      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;
      const decodedToken = jwtDecode(token);
      const employeeId = decodedToken.employeeId;

      const requestData = {
        shiftRequestedName: shiftName,
        startTime: formatTime(startTime),
        endTime: formatTime(endTime),
      };

      console.log("Sending request data:", requestData);

      const response = await axios.post(`http://localhost:8762/shift-requests/create/${employeeId}`, requestData, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response && response.data && !response.data.success) {
        throw new Error(response.data.message || "Failed to submit shift request.");
      }

      fetchPendingRequest();

      setShiftName("DAYSHIFT");
      setStartTime("");
      setEndTime("");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-5 p-5 bg-light rounded shadow-lg text-center" style={{ maxWidth: "700px" }}>
      <h2 className="text-primary fw-bold mb-4">Request Shift Change</h2>

      {loading && <p className="text-warning fw-bold">Processing request...</p>}
      {error && <p className="text-danger fw-bold">{error}</p>}

      <form onSubmit={handleSubmit} className="d-flex flex-column gap-3 mt-4">
        <div className="d-flex align-items-center justify-content-between w-100">
          <label className="fw-bold w-30 text-start">Shift Name:</label>
          <select
            className="form-select"
            style={{ width: "70%", height: "45px", borderRadius: "10px", padding: "10px" }}
            value={shiftName}
            onChange={(e) => setShiftName(e.target.value)}
          >
            <option value="DAYSHIFT">DAYSHIFT</option>
            <option value="NIGHTSHIFT">NIGHTSHIFT</option>
          </select>
        </div>

        <div className="d-flex align-items-center justify-content-between w-100">
          <label className="fw-bold w-30 text-start">Start Time:</label>
          <input
            className="form-control"
            style={{ width: "70%", height: "45px", borderRadius: "10px", padding: "10px" }}
            type="time"
            value={startTime}
            onChange={(e) => setStartTime(e.target.value)}
            required
          />
        </div>

        <div className="d-flex align-items-center justify-content-between w-100">
          <label className="fw-bold w-30 text-start">End Time:</label>
          <input
            className="form-control"
            style={{ width: "70%", height: "45px", borderRadius: "10px", padding: "10px" }}
            type="time"
            value={endTime}
            onChange={(e) => setEndTime(e.target.value)}
            required
          />
        </div>

        <button type="submit" className="btn btn-success fw-bold mt-3 px-5 py-3 rounded" disabled={loading}>
          Submit Request
        </button>
      </form>

      <h3 className="text-dark fw-bold mt-5">Your Pending Shift Request</h3>
      {loading ? (
        <p className="text-info">Loading pending request...</p>
      ) : error ? (
        <p className="text-danger fw-bold">{error}</p>
      ) : pendingRequest ? (
        <div className="table-responsive mt-3">
          <table className="table table-bordered">
            <thead className="table-dark">
              <tr>
                <th>Requested Shift</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>{pendingRequest.shiftRequestedName || "N/A"}</td>
                <td>{pendingRequest.startTime ? pendingRequest.startTime.slice(0, 5) : "N/A"}</td>
                <td>{pendingRequest.endTime ? pendingRequest.endTime.slice(0, 5) : "N/A"}</td>
                <td>
  <span style={{ 
    backgroundColor: pendingRequest.status === "APPROVED" ? "green" : 
                      pendingRequest.status === "REJECTED" ? "red" : 
                      pendingRequest.status === "PENDING" ? "yellow" : "gray",
    color: "black",
    padding: "5px",
    borderRadius: "5px",
    fontWeight: "bold"
  }}>
    {pendingRequest.status || "UNKNOWN"}
  </span>
</td>
              </tr>
            </tbody>
          </table>
        </div>
      ) : (
        <p className="fw-bold text-muted">No pending shift request found.</p>
      )}

      <div className="d-flex justify-content-between mt-5">
        <button className="btn btn-primary fw-bold px-5 py-3 rounded" onClick={() => navigate("/shifts")}>
          ← Back to Shift Dashboard
        </button>
        <button className="btn btn-secondary fw-bold px-5 py-3 rounded" onClick={() => navigate("/shifts/status")}>
          Check Shift Status →
        </button>
      </div>
    </div>
  );
};

export default ShiftRequest;
