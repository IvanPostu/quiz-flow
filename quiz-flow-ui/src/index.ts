import React from "react";
import { createRoot } from "react-dom/client";
import App from "./app/App";

import "normalize.css";
import "./styles/global.scss";

const container = document.getElementById("root");
const root = createRoot(container!);

root.render(React.createElement(App));
