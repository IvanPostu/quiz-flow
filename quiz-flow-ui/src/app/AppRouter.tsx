import {
  BrowserRouter,
  Navigate,
  Route,
  RouteProps,
  Router,
  Routes,
  useLocation,
} from "react-router-dom";
import { AboutPage } from "src/pages/AboutPage";
import { NotFoundPage } from "src/pages/NotFoundPage";
import { MainPage } from "../pages/MainPage";
import { QuizPage } from "src/pages/QuizPage";
import { DefaultLayout } from "src/components/Layout/DefaultLayout";
import { QuizLayout } from "src/components/Layout/QuizLayout";
import { SandboxPage } from "src/pages/SandboxPage";
import { SignInPage } from "src/pages/SignInPage";
import { SignUpPage } from "src/pages/SignUpPage";
import { selectIsAuthenticated } from "src/redux/authentication/authenticationSlice";
import { useAppSelector } from "src/redux";
import { JSX } from "react";
import { SignOutPage } from "src/pages/SignOutPage";
import { AuthenticationDetailsPage } from "src/pages/AuthenticationDetailsPage";
import { QuizSetupPage } from "src/pages/QuizSetupPage";
import { useTokenRefresher } from "src/hooks/useTokenRefresher";

const SIGN_IN_PATH = "/sign-in";
const SIGN_OUT_PATH = "/sign-out";

export const AppRouter = () => {
  useTokenRefresher();
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/quiz/:quizId" element={<QuizLayout />}>
          <Route index element={auth(<QuizPage />)} />
        </Route>
        <Route path="/" element={<DefaultLayout />}>
          <Route index element={auth(<MainPage />)} />
          <Route path="/about" element={auth(<AboutPage />)} />
          <Route path="/sandbox" element={auth(<SandboxPage />)} />
          <Route path="/setup-quiz" element={auth(<QuizSetupPage />)} />

          <Route path={SIGN_IN_PATH} element={<SignInPage />} />
          <Route path="/sign-up" element={<SignUpPage />} />
          <Route path={SIGN_OUT_PATH} element={auth(<SignOutPage />)} />
          <Route
            path={"/authentication-details"}
            element={auth(<AuthenticationDetailsPage />)}
          />
          <Route path="*" element={auth(<NotFoundPage />)} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

function auth(element: JSX.Element): JSX.Element {
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  if (isAuthenticated) {
    return element;
  }

  const pathname = window.location.pathname;
  const search = window.location.search;

  if (
    pathname === SIGN_IN_PATH ||
    pathname === SIGN_OUT_PATH ||
    pathname === "/"
  ) {
    return <Navigate to={SIGN_IN_PATH} />;
  }
  const params = new URLSearchParams({
    redirectTo: pathname + search,
  });
  return <Navigate to={`${SIGN_IN_PATH}?${params.toString()}`} />;
}
