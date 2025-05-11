import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import "./ManagerProfile.css";

const ManagerProfile = () => {
  const navigate = useNavigate();
  const [profileData, setProfileData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
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

  const employeeId = getEmployeeIdFromToken();

  useEffect(() => {
    console.log("Retrieved Employee ID:", employeeId);

    if (!employeeId || isNaN(Number(employeeId))) {
      setError("Invalid Employee ID. Please try again.");
      setLoading(false);
      return;
    }

    const fetchProfileData = async () => {
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

        const response = await fetch(`http://localhost:8092/employee/profile/${employeeId}`, {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Accept": "application/json",
          },
        });

        console.log("HTTP Response Status:", response.status);

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || `Server error: ${response.status} ${response.statusText}`);
        }

        const result = await response.json();
        console.log("Profile Data:", result);

        if (result.success && result.data) {
          setProfileData(result.data);
        } else {
          setError(result.message || "Failed to fetch profile.");
        }
      } catch (error) {
        console.error("Network/Server Error:", error);
        setError("Failed to fetch profile. Please check your connection.");
      } finally {
        setLoading(false);
      }
    };

    fetchProfileData();
  }, []);

  return (
    <div className="profile-container">
      <h2>Employee Profile</h2>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <p>Loading profile details...</p>
      ) : profileData ? (
        <div className="profile-card">
          <p><strong>Employee ID:</strong> {profileData.employeeId}</p>
          <p><strong>Name:</strong> {profileData.firstName} {profileData.lastName}</p>
          <p><strong>Gender:</strong> {profileData.gender}</p>
          <p><strong>Email:</strong> {profileData.email}</p>
          <p><strong>Role:</strong> {profileData.role}</p>
          <p><strong>Department:</strong> {profileData.department || "N/A"}</p>
        </div>
      ) : (
        <p>No profile data available.</p>
      )}

      <button className="back-button" onClick={() => navigate("/dashboard")}>
        ← Back to Dashboard
      </button>
    </div>
  );
};

export default ManagerProfile;
