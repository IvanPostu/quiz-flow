import counterReducer from "../counter/counterSlice";
import authenticationReducer from "../authentication/authenticationSlice";
import { combineReducers } from "@reduxjs/toolkit";

const rootReducer = combineReducers({
  counter: counterReducer,
  authentication: authenticationReducer,
});

export default rootReducer;
