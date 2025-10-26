import { useEffect } from "react";
import { Navigate } from "react-router-dom";
import { SignOutContainer } from "src/components/SignOutContainer/SignOutContainer";
import { signOut } from "src/model/authentication/authentication";
import { useAppDispatch, useAppSelector } from "src/redux";
import {
  clearAuthentication,
  selectIsAuthenticated,
} from "src/redux/authentication/authenticationSlice";

export const SignOutPage = () => {
  const dispatch = useAppDispatch();
  const isAuthenticated = useAppSelector(selectIsAuthenticated);

  useEffect(() => {
    performSignOut(() => {
      dispatch(clearAuthentication());
    });
  }, []);

  if (isAuthenticated) {
    return <SignOutContainer />;
  }
  return <Navigate to={"/"} />;
};

async function performSignOut(onSignOutComplete: () => void) {
  try {
    await signOut();
    onSignOutComplete();
  } catch (e) {
    console.error(`Sign out request failed due to: ${e}`);
    onSignOutComplete();
  }
}
