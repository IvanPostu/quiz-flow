import React from "react";
import { createRoot } from "react-dom/client";
import App from "./app/App";

import "normalize.css";
import "./styles.css";
import "./styles/styles.scss";

const container = document.getElementById("root");
const root = createRoot(container!);

root.render(React.createElement(App));
