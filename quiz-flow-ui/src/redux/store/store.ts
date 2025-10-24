import { configureStore } from "@reduxjs/toolkit";
import counterReducer from "../counter/counterSlice";
import authenticationReducer from "../authentication/authenticationSlice";
import { useDispatch, useSelector } from "react-redux";

export const store = configureStore({
  reducer: {
    counter: counterReducer,
    authentication: authenticationReducer,
  },
  devTools: process.env.NODE_ENV !== "production",
});

// see https://react-redux.js.org/tutorials/typescript-quick-start
// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>;
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch;

export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
export const useAppSelector = useSelector.withTypes<RootState>();
