import React from "react";
import { useNavigate } from "react-router-dom";
import "./IndividualReports.css";

const IndividualReports = () => {
  const navigate = useNavigate(); // Initialize navigate function

  return (
    <div className="reports-container">
      <h2>Reports Dashboard</h2>
      <div className="buttons-container">
        <button onClick={() => navigate("/reports/attendance")}>Attendance Reports</button>
        <button onClick={() => navigate("/reports/leaves")}>Leave Reports</button>
        <button onClick={() => navigate("/reports/shifts")}>Shift Reports</button>
      </div>
    </div>
  );
};

export default IndividualReports;
