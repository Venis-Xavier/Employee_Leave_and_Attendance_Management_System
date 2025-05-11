import React from "react";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";

const ShiftDashboard = () => {
  const navigate = useNavigate();

  return (
    <div className="container text-center py-5">
      <h2 className="display-4 text-primary fw-bold mb-4">Shift Dashboard</h2>
      <div className="d-flex justify-content-center gap-4 mt-5">
        <button
          className="btn btn-lg btn-success rounded-pill px-5 py-3"
          onClick={() => navigate("/shifts/request")}
          style={{ fontSize: "1.1rem" }}
        >
          Request Shift Change
        </button>
        <button
          className="btn btn-lg btn-info rounded-pill px-5 py-3 text-white"
          onClick={() => navigate("/shifts/status")}
          style={{ fontSize: "1.1rem" }}
        >
          View Shift Requests
        </button>
        <button
          className="btn btn-lg btn-warning rounded-pill px-5 py-3"
          onClick={() => navigate("/shifts/assigned")}
          style={{ fontSize: "1.1rem" }}
        >
          View All Assigned Shifts
        </button>
      </div>
    </div>
  );
};

export default ShiftDashboard;
