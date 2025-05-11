import React, { useState } from "react";
import { jwtDecode } from "jwt-decode"; // Import JWT decoding
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css"; // Import Bootstrap CSS
import './ManagerDashboard.css'; // Import custom CSS
 
const ManagerDashboard = ({ setLoggedIn }) => {
  const navigate = useNavigate();
  const [clockInTime, setClockInTime] = useState("");
  const [clockOutTime, setClockOutTime] = useState("");
  const [date, setDate] = useState("");
  const [clockoutdate, setclockoutdate] = useState("");
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
 
  const assignedShift = {
    shiftName: "Morning Shift",
    assignedClockIn: "09:00 AM",
    assignedClockOut: "05:00 PM",
  };
 
  const formatForBackend = (dateStr, timeStr) => {
    return dateStr && timeStr ? `${dateStr}T${timeStr}:00` : null;
  };
 
  const handleLogOut = () => {
    localStorage.removeItem("userLoggedIn");
    localStorage.removeItem("jwtToken");
    setLoggedIn(false);
    navigate("/");
  };
 
  const submitAttendance = async (event) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);
 
    const tokenObject = localStorage.getItem("jwtToken");
    if (!tokenObject) {
      setError("No JWT token found. Please log in.");
      return;
    }
 
    // ✅ Extract actual token from stored object
    const parsedToken = JSON.parse(tokenObject);
    const token = parsedToken.data; // Extracting the JWT token string
 
    try {
      const decodedToken = jwtDecode(token);
      const employeeId = decodedToken.employeeId;
      console.log("Extracted Employee ID:", employeeId);
 
      if (!employeeId) {
        setError("Employee ID missing or invalid.");
        return;
      }
 
      const attendanceData = {
        clockInTime: formatForBackend(date, clockInTime),
        clockOutTime: formatForBackend(clockoutdate, clockOutTime),
      };
 
      console.log("Request payload:", attendanceData);
 
      const response = await fetch(`http://localhost:3001/attendance/giveattendance/${employeeId}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        credentials: "include",
        body: JSON.stringify(attendanceData),
      });
 
      console.log("Raw response:", response);
 
      const responseText = await response.text();
      const responseBody = responseText ? JSON.parse(responseText) : {};
 
      console.log("Successful response:", responseBody);
 
      if (!response.ok || !responseBody.success) {
        const errorMessage = responseBody.message || `Server error: ${response.status} ${response.statusText}`;
        throw new Error(errorMessage);
      }
 
      setSuccess(responseBody.message || "Attendance submitted successfully!");
    } catch (error) {
      console.error("Full error posting attendance:", error);
      setError(error.message || "Failed to submit attendance. Please check console for details.");
    }
  };
 
  return (
    <div className="container-fluid bg-light py-5 d-flex justify-content-center align-items-center min-vh-100">
      <div className="container">
        <h2 className="mb-4 text-center">Manager Dashboard</h2>
        <div className="row justify-content-center">
          <div className="col-md-8 col-lg-6">
            {error && <div className="alert alert-danger mb-3">{error}</div>}
            {success && <div className="alert alert-success mb-3">{success}</div>}
 
            <form onSubmit={submitAttendance} className="bg-white rounded shadow p-5 mb-4">
              <div className="mb-4 row align-items-center">
                <label htmlFor="date" className="col-sm-4 col-form-label fs-5">
                  Date
                </label>
                <div className="col-sm-8">
                  <input
                    type="date"
                    className="form-control form-control-lg"
                    id="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                    required
                  />
                </div>
              </div>
 
              <div className="mb-4 row align-items-center">
                <label htmlFor="clockInTime" className="col-sm-4 col-form-label fs-5">
                  Clock In Time
                </label>
                <div className="col-sm-8">
                  <input
                    type="time"
                    className="form-control form-control-lg"
                    id="clockInTime"
                    value={clockInTime}
                    onChange={(e) => setClockInTime(e.target.value)}
                  />
                </div>
              </div>
 
              <div className="mb-4 row align-items-center">
                <label htmlFor="clockOutDate" className="col-sm-4 col-form-label fs-5">
                  Date
                </label>
                <div className="col-sm-8">
                  <input
                    type="date"
                    className="form-control form-control-lg"
                    id="clockOutDate"
                    value={clockoutdate}
                    onChange={(e) => setclockoutdate(e.target.value)}
                    required
                  />
                </div>
              </div>
 
              <div className="mb-4 row align-items-center">
                <label htmlFor="clockOutTime" className="col-sm-4 col-form-label fs-5">
                  Clock Out Time
                </label>
                <div className="col-sm-8">
                  <input
                    type="time"
                    className="form-control form-control-lg"
                    id="clockOutTime"
                    value={clockOutTime}
                    onChange={(e) => setClockOutTime(e.target.value)}
                  />
                </div>
              </div>
 
              <div className="d-flex justify-content-center">
                <button type="submit" className="btn btn-primary btn-lg w-50">
                  Submit Attendance
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};
 
export default ManagerDashboard;