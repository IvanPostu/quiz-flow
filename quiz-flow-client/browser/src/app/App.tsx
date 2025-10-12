import { Route } from "react-router-dom";
import { Fragment } from "react/jsx-runtime";
import { AboutPage } from "src/pages/AboutPage";
import { NotFoundPage } from "src/pages/NotFoundPage";
import { MainPage } from "../pages/MainPage";
import { ApplicationRoutes } from "./ApplicationRoutes";

const App = () => {
  return (
    <Fragment>
      <ApplicationRoutes>
        <Route index element={<MainPage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </ApplicationRoutes>
    </Fragment>
  );
};

export default App;
