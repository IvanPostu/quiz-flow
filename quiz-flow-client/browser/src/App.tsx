import { Fragment } from "react/jsx-runtime";
import { AboutPage } from "src/pages/AboutPage";
import { MainPage } from "./pages/MainPage";

const App = () => {
  return (
    <Fragment>
      <div>App</div>
      <div>
        <MainPage></MainPage>
        <AboutPage></AboutPage>
      </div>
    </Fragment>
  );
};

export default App;
