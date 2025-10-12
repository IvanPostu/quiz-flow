import { PropsWithChildren } from "react";
import { Route, BrowserRouter as Router, Routes } from "react-router-dom";
import { DefaultLayout } from "src/components/Layout/DefaultLayout";

export const ApplicationRoutes = (props: PropsWithChildren) => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<DefaultLayout />}>
          {props.children}
        </Route>
      </Routes>
    </Router>
  );
};
