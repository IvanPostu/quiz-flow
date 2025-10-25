import {
  BrowserRouter,
  Navigate,
  Route,
  RouteProps,
  Router,
  Routes,
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

const SIGN_IN_PATH = "/sign-in";

export const AppRouter = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/quiz" element={<QuizLayout />}>
          <Route index element={auth(<QuizPage />)} />
        </Route>
        <Route path="/" element={<DefaultLayout />}>
          <Route index element={auth(<MainPage />)} />
          <Route path="/about" element={auth(<AboutPage />)} />
          <Route path="/sandbox" element={auth(<SandboxPage />)} />

          <Route path={SIGN_IN_PATH} element={<SignInPage />} />
          <Route path="/sign-up" element={<SignUpPage />} />
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
  return <Navigate to={SIGN_IN_PATH} />;
}
