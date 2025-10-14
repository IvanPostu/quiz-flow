import { Outlet } from "react-router-dom";
import * as styles from "./styles.module.scss";
import { Sidebar } from "src/components/Sidebar/Sidebar";
import { useState } from "react";
import { Navbar } from "src/components/Navbar/Navbar";

export const DefaultLayout = () => {
  const [state, setState] = useState({
    sidebarIsShown: false,
  });

  return (
    <div className={styles.layoutRoot}>
      <LayoutSidebar sidebarIsShown={state.sidebarIsShown} />
      <div className={styles.main}>
        <Navbar
          triggerSidebar={() => {
            setState((prevState) => ({
              ...prevState,
              sidebarIsShown: !prevState.sidebarIsShown,
            }));
          }}
        />
        <main className={styles.content}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};

function LayoutSidebar(props: { sidebarIsShown: boolean }) {
  return (
    <Sidebar
      sidebarIsShown={props.sidebarIsShown}
      elements={[
        {
          headerText: "Pages",
          items: [
            {
              text: "Main",
              icon: "home",
            },
            {
              text: "Profile",
              icon: "person",
            },
            {
              text: "Task",
              icon: "document",
            },
            {
              header: {
                text: "Auth",
                icon: "shield",
              },
              items: ["Sign-In", "Sign-Up"],
            },
          ],
        },
        {
          headerText: "Tools & Components",
          items: [
            {
              header: {
                icon: "layers",
                text: "Form Elements",
              },
              items: ["Accordion", "Tabs"],
            },
            {
              text: "Notifications",
            },
          ],
        },
      ]}
    />
  );
}
