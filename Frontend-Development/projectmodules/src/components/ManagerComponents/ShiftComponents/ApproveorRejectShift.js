import React, { useState, useEffect } from "react";
import "./ApproveorRejectShift.css";
import { jwtDecode } from "jwt-decode";

const ApproveorRejectShift = () => {
  const [selectedFilter, setSelectedFilter] = useState(null);
  const [approvedRejectedRequests, setApprovedRejectedRequests] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  //  Extract managerId from JWT token
  const getManagerIdFromToken = () => {
    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) throw new Error("No JWT token found. Please log in.");

      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;
      const decodedToken = jwtDecode(token);

      console.log("Extracted Manager ID from JWT:", decodedToken?.employeeId);
      return decodedToken?.employeeId || null;
    } catch (error) {
      console.error("Error decoding JWT token:", error);
      return null;
    }
  };

  const managerId = getManagerIdFromToken();
  console.log("Manager ID Used in API Fetch:", managerId);

  // Fetch approved/rejected requests dynamically from backend
  useEffect(() => {
    if (!managerId) {
      console.error("Manager ID is missing, skipping API call.");
      return;
    }

    const fetchApprovedRejectedShiftRequests = async () => {
      setLoading(true);
      setError(null);

      try {
        const tokenObject = localStorage.getItem("jwtToken");
        if (!tokenObject) throw new Error("No JWT token found. Please log in.");

        const parsedToken = JSON.parse(tokenObject);
        const token = parsedToken.data;

        const apiUrl = `http://localhost:8762/updatedrequests/managerId/${managerId}`;
        console.log("Fetching Approved/Rejected Shift Requests from:", apiUrl);

        const response = await fetch(apiUrl, {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Accept": "application/json",
          },
        });

        if (!response.ok) throw new Error(`Error fetching shift requests: ${response.status}`);

        const result = await response.json();
        console.log("Raw Shift Request Data:", result);

        if (result.success && Array.isArray(result.data)) {
          setApprovedRejectedRequests(result.data.flat()); // Flatten nested arrays
        } else {
          throw new Error(result.message || "Invalid response format.");
        }
      } catch (error) {
        console.error("Error fetching shift requests:", error);
        setError(error.message);
      } finally {
        setLoading(false);
      }
    };

    fetchApprovedRejectedShiftRequests();
  }, [managerId]);

  //  Fetch pending shift requests separately
  useEffect(() => {
    if (!managerId) {
      console.error("Manager ID is missing, skipping API call.");
      return;
    }

    const fetchPendingShiftRequests = async () => {
      setLoading(true);
      setError(null);

      try {
        const tokenObject = localStorage.getItem("jwtToken");
        if (!tokenObject) throw new Error("No JWT token found. Please log in.");

        const parsedToken = JSON.parse(tokenObject);
        const token = parsedToken.data;

        const apiUrl = `http://localhost:8762/shift-requests/all/${managerId}`;
        console.log("Fetching Pending Shift Requests from:", apiUrl);

        const response = await fetch(apiUrl, {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Accept": "application/json",
          },
        });

        if (!response.ok) throw new Error(`Error fetching shift requests: ${response.status}`);

        const result = await response.json();
        console.log("Raw Pending Shift Request Data:", result);

        if (result.success && Array.isArray(result.data)) {
          setPendingRequests(result.data);
        } else {
          throw new Error(result.message || "Invalid response format.");
        }
      } catch (error) {
        console.error("Error fetching pending shift requests:", error);
        setError(error.message);
      } finally {
        setLoading(false);
      }
    };

    fetchPendingShiftRequests();
  }, [managerId]);

  //  Function to handle approving a shift request
  const handleApprove = async (employeeId) => {
    setLoading(true);
    setError(null);
    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) throw new Error("No JWT token found. Please log in.");
      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;

      const apiUrl = `http://localhost:8762/shift-requests/shiftApprovalOrReject/${employeeId}`;
      const response = await fetch(apiUrl, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ status: "APPROVED" }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error approving request: ${response.status}`);
      }

      // Update the state to reflect the approval
      setPendingRequests(pendingRequests.filter((request) => request.employeeId !== employeeId));
      // Optionally, you might want to refetch approved/rejected requests to update that list
    } catch (error) {
      console.error("Error approving shift request:", error);
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  //  Function to handle rejecting a shift request
  const handleReject = async (employeeId) => {
    setLoading(true);
    setError(null);
    try {
      const tokenObject = localStorage.getItem("jwtToken");
      if (!tokenObject) throw new Error("No JWT token found. Please log in.");
      const parsedToken = JSON.parse(tokenObject);
      const token = parsedToken.data;

      const apiUrl = `http://localhost:8762/shift-requests/shiftApprovalOrReject/${employeeId}`;
      const response = await fetch(apiUrl, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ status: "REJECTED" }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error rejecting request: ${response.status}`);
      }

      // Update the state to reflect the rejection
      setPendingRequests(pendingRequests.filter((request) => request.employeeId !== employeeId));
      // Optionally, you might want to refetch approved/rejected requests to update that list
    } catch (error) {
      console.error("Error rejecting shift request:", error);
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="shift-requests-container">
      <h2>Manage Shift Requests</h2>

      {error && <div className="error-message">{error}</div>}
      {loading && <p>Loading shift requests...</p>}

      {/* Filter Buttons */}
      <div className="button-group">
        <button onClick={() => setSelectedFilter("APPROVED")}>Approved Shift Requests</button>
        <button onClick={() => setSelectedFilter("REJECTED")}>Rejected Shift Requests</button>
        <button onClick={() => setSelectedFilter("PENDING")}>Pending Shift Requests</button>
      </div>

      {/*  Keep Approved & Rejected Requests Unchanged */}
      {selectedFilter && selectedFilter !== "PENDING" && (
        <table className="shift-requests-table">
          <thead>
            <tr>
              <th>Employee ID</th>
              <th>Shift Requested Name</th>
              <th>Start Time</th>
              <th>End Time</th>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {approvedRejectedRequests
              .filter((request) => request.status === selectedFilter)
              .map((request) => (
                <tr key={request.updatesId}>
                  <td>{request.employeeId}</td>
                  <td>{request.shiftRequestedName}</td>
                  <td>{request.startTime}</td>
                  <td>{request.endTime}</td>
                  <td>{request.startDate || "N/A"}</td>
                  <td>{request.endDate || "N/A"}</td>
                  <td>{request.status}</td>
                </tr>
              ))}
          </tbody>
        </table>
      )}

      {/*  Updated Pending Requests Section with Approve/Reject Buttons */}
      {selectedFilter === "PENDING" && (
        <table className="shift-requests-table">
          <thead>
            <tr>
              <th>Employee ID</th>
              <th>Shift Requested Name</th>
              <th>Assigned Shift Name</th>
              <th>Start Time</th>
              <th>End Time</th>
              <th>Request Timestamp</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {pendingRequests.length > 0 ? (
              pendingRequests.map((request) => (
                <tr key={request.shiftRequestedId}>
                  <td>{request.employeeId}</td>
                  <td>{request.shiftRequestedName}</td>
                  <td>{request.assignedShiftName}</td>
                  <td>{request.startTime}</td>
                  <td>{request.endTime}</td>
                  <td>{new Date(request.timestamp).toLocaleString()}</td>
                  <td>
                    <button
                      className="approve-button"
                      onClick={() => handleApprove(request.employeeId)}
                      disabled={loading}
                    >
                      Approve
                    </button>
                    <button
                      className="reject-button"
                      onClick={() => handleReject(request.employeeId)}
                      disabled={loading}
                    >
                      Reject
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="7" style={{ textAlign: "center", fontWeight: "bold" }}>
                  No pending shift requests found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default ApproveorRejectShift;