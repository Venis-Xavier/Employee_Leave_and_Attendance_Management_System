import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";

const LeaveDashboard = () => {
  const navigate = useNavigate();
  const [leaveStatus, setLeaveStatus] = useState("Pending");
  const [leaveBalance, setLeaveBalance] = useState({ casual: 5, sick: 3 });

  const primaryColor = "#2f3e9e";
  const secondaryColor = "#4054b2";
  const successColor = "#388e3c";
  const backgroundColor = "#f4f7fc";
  const textColor = "#fff";

  const handleApplyLeave = () => navigate("/leaves/apply");
  const handleViewStatus = () => navigate("/leaves/status");
  const handleCheckBalance = () => navigate("/leaves/balance");

  return (
    <div className="container-fluid d-flex flex-column align-items-center justify-content-center min-vh-100 p-4" style={{ backgroundColor }}>
      <h2 className="text-center mb-4" style={{ fontSize: '2rem', fontWeight: 'bold', color: primaryColor }}>Leave Dashboard</h2>

      <div className="row w-100 justify-content-center" style={{ maxWidth: '400px' }}>
        <div className="col-12 mb-3">
          <button
            className="btn btn-primary btn-lg w-100 rounded shadow"
            onClick={handleApplyLeave}
            style={{
              backgroundColor: primaryColor,
              borderColor: primaryColor,
              color: textColor,
              padding: '1rem',
              fontSize: '1rem',
              transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out'
            }}
          >
            Apply Leave
          </button>
        </div>

        <div className="col-12 mb-3">
          <button
            className="btn btn-info btn-lg w-100 rounded shadow"
            onClick={handleViewStatus}
            style={{
              backgroundColor: secondaryColor,
              borderColor: secondaryColor,
              color: textColor,
              padding: '1rem',
              fontSize: '1rem',
              transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out'
            }}
          >
            Leave Status
          </button>
        </div>

        <div className="col-12 mb-3">
          <button
            className="btn btn-success btn-lg w-100 rounded shadow"
            onClick={handleCheckBalance}
            style={{
              backgroundColor: successColor,
              borderColor: successColor,
              color: textColor,
              padding: '1rem',
              fontSize: '1rem',
              transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out'
            }}
          >
            Leave Balance
          </button>
        </div>
      </div>
    </div>
  );
};

export default LeaveDashboard;
