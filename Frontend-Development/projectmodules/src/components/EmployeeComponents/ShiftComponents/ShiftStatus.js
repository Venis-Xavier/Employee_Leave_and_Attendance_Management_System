import React, { useEffect, useState } from "react";
import axios from "axios";
import "./ShiftStatus.css";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

const ShiftStatus = () => {
  const navigate = useNavigate();
  const [shiftRequests, setShiftRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchShiftRequests = async () => {
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

        console.log(`Fetching shift requests for Employee ID: ${employeeId}`);

        const response = await axios.get(`http://localhost:8762/updatedrequests/${employeeId}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        console.log("API Response:", response);

        if (!response || !response.data) {
          throw new Error("Unexpected API response format: No data received.");
        }

        // Handle cases where response.data might be the array directly or nested
        const requestsData = Array.isArray(response.data) ? response.data : (response.data.data || []);

        if (!Array.isArray(requestsData)) {
          console.warn("API response 'data' is not an array:", response.data);
          setShiftRequests([]);
          return;
        }

        const formattedData = requestsData.map((request) => ({
          shiftName: request.shiftRequestedName || "N/A",
          startDate: request.startDate || "N/A",
          endDate: request.endDate || "N/A",
          startTime: request.startTime || "N/A",
          endTime: request.endTime || "N/A",
          status: request.status || "PENDING", // Default to PENDING if status is not provided
        }));

        setShiftRequests(formattedData);
      } catch (err) {
        console.error("Error fetching shift requests:", err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchShiftRequests();
  }, []);

  return (
    <div className="shift-status-container">
      <h2>Previous Shift Requests</h2>

      {loading ? (
        <p>Loading...</p>
      ) : error ? (
        <p className="error-message">Error: {error}</p>
      ) : shiftRequests.length > 0 ? (
        <table>
          <thead>
            <tr>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Start Time</th>
              <th>End Time</th>
              <th>Requested Shift Name</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {shiftRequests.map((shift, idx) => (
              <tr key={idx}>
                <td>{shift.startDate !== "N/A" ? new Date(shift.startDate).toLocaleDateString() : "N/A"}</td>
                <td>{shift.endDate !== "N/A" ? new Date(shift.endDate).toLocaleDateString() : "N/A"}</td>
                <td>{shift.startTime !== "N/A" ? shift.startTime.slice(0, 5) : "N/A"}</td> {/* Display only HH:MM */}
                <td>{shift.endTime !== "N/A" ? shift.endTime.slice(0, 5) : "N/A"}</td>   {/* Display only HH:MM */}
                <td>{shift.shiftName}</td>
                <td>
                  <span className={`status ${shift.status.toLowerCase()}`}>
                    {shift.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>No shift requests found.</p>
      )}

      <div className="bottom-buttons">
        <button className="back-button" onClick={() => navigate("/shifts")}>
          ← Back to Shift Dashboard
        </button>
      </div>
    </div>
  );
};

export default ShiftStatus;