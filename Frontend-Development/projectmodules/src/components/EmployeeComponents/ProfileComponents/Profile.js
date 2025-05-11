import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import "bootstrap/dist/css/bootstrap.min.css"; // Added Bootstrap

const Profile = () => {
  const { employeeId: urlEmployeeId } = useParams();
  const navigate = useNavigate();
  const [profileData, setProfileData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  // Retrieve employeeId from JWT token
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
    <div style={{ maxWidth: "650px", margin: "50px auto", padding: "30px", borderRadius: "10px", boxShadow: "0 5px 10px rgba(0, 0, 0, 0.15)", backgroundColor: "white", textAlign: "center" }}>
      <h2 style={{ color: "#333", marginBottom: "20px" }}>Employee Profile</h2>

      {error && <div style={{ color: "red", fontWeight: "bold", marginBottom: "20px" }}>{error}</div>}

      {loading ? (
        <p style={{ fontSize: "18px", fontWeight: "bold" }}>Loading profile details...</p>
      ) : profileData ? (
        <div style={{ padding: "25px", backgroundColor: "#f9f9f9", borderRadius: "10px", boxShadow: "0 3px 6px rgba(0, 0, 0, 0.1)", textAlign: "left" }}>
          <p style={{ marginBottom: "12px", fontSize: "17px" }}><strong>Employee ID:</strong> {profileData.employeeId}</p>
          <p style={{ marginBottom: "12px", fontSize: "17px" }}><strong>Manager ID:</strong> {profileData.managerId || "N/A"}</p>
          <p style={{ marginBottom: "12px", fontSize: "17px" }}><strong>Name:</strong> {profileData.firstName} {profileData.lastName}</p>
          <p style={{ marginBottom: "12px", fontSize: "17px" }}><strong>Gender:</strong> {profileData.gender}</p>
          <p style={{ marginBottom: "12px", fontSize: "17px" }}><strong>Email:</strong> {profileData.email}</p>
          <p style={{ marginBottom: "12px", fontSize: "17px" }}><strong>Role:</strong> {profileData.role}</p>
          <p style={{ marginBottom: "12px", fontSize: "17px" }}><strong>Department:</strong> {profileData.department || "N/A"}</p>
        </div>
      ) : (
        <p style={{ fontSize: "18px", fontWeight: "bold" }}>No profile data available.</p>
      )}

      <button style={{ marginTop: "25px", padding: "12px 20px", backgroundColor: "#007bff", color: "white", border: "none", borderRadius: "6px", cursor: "pointer", fontSize: "17px", fontWeight: "bold", transition: "background-color 0.2s ease-in-out" }}
        onClick={() => navigate("/dashboard")}
        onMouseEnter={(e) => e.target.style.backgroundColor = "#0056b3"}
        onMouseLeave={(e) => e.target.style.backgroundColor = "#007bff"}>
        ← Back to Dashboard
      </button>
    </div>
  );
};

export default Profile;
