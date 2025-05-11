import React from "react";
import { useNavigate } from "react-router-dom";
import "./LeaveRequests.css";

const LeaveRequests = () => {
  const navigate = useNavigate();

  return (
    <div className="leave-requests-container">
      <h2>Manage Leave Requests</h2>

      <div className="button-group">
        {/* ✅ Button to approve/reject leave requests */}
        <button onClick={() => navigate("/approve-reject-leave")}>Approve / Reject Leave</button>

        {/* ✅ Button to check employee leave balance */}
        <button onClick={() => navigate("/leave-balance")}>Check Leave Balance</button>
      </div>
    </div>
  );
};

export default LeaveRequests;
