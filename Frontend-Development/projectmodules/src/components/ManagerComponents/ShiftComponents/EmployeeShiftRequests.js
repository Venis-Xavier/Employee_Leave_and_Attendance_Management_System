import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./EmployeeShiftRequests.css";

import AssignShifts from "./AssignShifts"; // ✅ Import AssignShift component
import AllAssignments from "./AllAssignments"; // ✅ Import AllAssignments component

const EmployeeShiftRequests = () => {
  const navigate = useNavigate();
  const [assignments, setAssignments] = useState([]); // ✅ Maintain shift assignments
  const [showAssignForm, setShowAssignForm] = useState(false);
  const [showAssignments, setShowAssignments] = useState(false); // ✅ Toggle display for assignments

  return (
    <div className="shift-requests-container">
      <h2>Manage Shift Requests</h2>

      <div className="button-group">
        <button onClick={() => navigate("/approve-reject-requests")}>Show All Requests</button>
        <button onClick={() => navigate("/show-assignments")}>Show All Assignments</button> {/* ✅ Toggle Assignments */}
        <button onClick={() => setShowAssignForm(!showAssignForm)}>Assign Shift</button> {/* ✅ Toggle Assign Shift form */}
      </div>

      {/* ✅ Display Assign Shift form only when clicked */}
      {showAssignForm && <AssignShifts setAssignments={setAssignments} />}
      
      {/* ✅ Display Assignments only when Show All Assignments is clicked */}
      {showAssignments && <AllAssignments assignments={assignments} />}
    </div>
  );
};

export default EmployeeShiftRequests;
