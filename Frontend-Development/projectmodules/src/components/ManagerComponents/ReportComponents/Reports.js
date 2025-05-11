import React from "react";
import { useNavigate } from "react-router-dom";
import "./Reports.css";

const Reports = () => {
  const navigate = useNavigate(); // Initialize navigate function

  return (
    <div className="reports-container">
      <h2>Reports Dashboard</h2>
      <div className="buttons-container">
        <button onClick={() => navigate("/attendance-reports")}>Attendance Reports</button>
        <button onClick={() => navigate("/leave-reports")}>Leave Reports</button>
        <button onClick={() => navigate("/shift-reports")}>Shift Reports</button>
      </div>
    </div>
  );
};

export default Reports;
