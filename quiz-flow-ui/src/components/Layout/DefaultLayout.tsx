import { Outlet } from "react-router-dom";
import * as styles from "./styles.module.scss";
import { Sidebar } from "src/components/Sidebar/Sidebar";
import { useState } from "react";
import { Navbar } from "src/components/Navbar/Navbar";
import { useAppSelector } from "src/redux";
import { selectIsAuthenticated } from "src/redux/authentication/authenticationSlice";

const AUTH_ITEMS_FOR_AUTHENTICATED = [
  { text: "Details", link: "/authentication-details" },
  { text: "Sign-Out", link: "/sign-out" },
];

const AUTH_ITEMS_FOR_UNAUTHENTICATED = [
  { text: "Sign-In", link: "/sign-in" },
  { text: "Sign-Up", link: "/sign-up" },
];

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
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
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
              link: "/",
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
              items: isAuthenticated
                ? AUTH_ITEMS_FOR_AUTHENTICATED
                : AUTH_ITEMS_FOR_UNAUTHENTICATED,
            },
          ],
        },
        {
          headerText: "Tools & Components",
          items: [
            {
              header: {
                icon: "hammer",
                text: "Development",
              },
              items: [
                { text: "Accordion" },
                { text: "Quiz Demo", link: "/quiz" },
                { text: "Sandbox", link: "/sandbox" },
              ],
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
