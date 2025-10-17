import { BrowserRouter, Route, Router, Routes } from "react-router-dom";
import { AboutPage } from "src/pages/AboutPage";
import { NotFoundPage } from "src/pages/NotFoundPage";
import { MainPage } from "../pages/MainPage";
import { QuizPage } from "src/pages/QuizPage";
import { DefaultLayout } from "src/components/Layout/DefaultLayout/DefaultLayout";
import { QuizLayout } from "src/components/Layout/QuizLayout/QuizLayout";
import { Provider } from "react-redux";
import { store } from "src/redux/store/store";
import { SandboxPage } from "src/pages/SandboxPage";
import { SignInPage } from "src/pages/SignInPage";
import { SignUpPage } from "src/pages/SignUpPage";

const App = () => {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <Routes>
          <Route path="/quiz/" element={<QuizLayout />}>
            <Route index element={<QuizPage />} />
          </Route>
          <Route path="/" element={<DefaultLayout />}>
            <Route index element={<MainPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/sandbox" element={<SandboxPage />} />
            <Route path="/sign-in" element={<SignInPage />} />
            <Route path="/sign-up" element={<SignUpPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </Provider>
  );
};

export default App;
