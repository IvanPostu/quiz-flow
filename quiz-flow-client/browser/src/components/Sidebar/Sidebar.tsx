import { Fragment } from "react/jsx-runtime";
import * as styles from "./styles.module.scss";
import { useState } from "react";
import {
  IoDocumentOutline,
  IoPersonOutline,
  IoShieldOutline,
  IoLayersOutline,
  IoHomeOutline,
  IoHammerOutline,
} from "react-icons/io5";
import { Link } from "react-router-dom";

type SidebarIconType =
  | "document"
  | "person"
  | "home"
  | "shield"
  | "layers"
  | "hammer";

interface SidebarItem {
  text: string;
  link?: string;
  icon?: SidebarIconType;
}

type SidebarDropdown = {
  header: string | SidebarItem;
  items: Array<SidebarItem>;
};

type SidebarElement = {
  headerText: string;
  items: Array<SidebarItem | SidebarDropdown>;
};

type SidebarPropsType = {
  sidebarIsShown: boolean;
  elements: SidebarElement[];
};

function isSidebarItem(obj: any): obj is SidebarItem {
  return obj && typeof obj.text === "string";
}

function isSidebarDropdown(obj: any): obj is SidebarDropdown {
  return obj && Boolean(obj.header);
}

const ICON_BY_TYPE: Record<SidebarIconType, React.ReactElement> = {
  document: <IoDocumentOutline className={styles.icon} />,
  person: <IoPersonOutline className={styles.icon} />,
  home: <IoHomeOutline className={styles.icon} />,
  shield: <IoShieldOutline className={styles.icon} />,
  layers: <IoLayersOutline className={styles.icon} />,
  hammer: <IoHammerOutline className={styles.icon} />,
};

export const Sidebar = (props: SidebarPropsType) => {
  const [state, setState] = useState({
    activeDropdownPaths: new Set<string>(),
  });

  return (
    <aside
      className={`${styles.sidebarRoot} ${
        props.sidebarIsShown ? "" : styles.sidebarCollapsed
      }`}
    >
      <div className={styles.sidebarContainer}>
        <div className={styles.sidebarLogo}>
          <Link to="/">Quiz Flow</Link>
        </div>
        <ul className={styles.sidebarNav}>
          {props.elements.map((element) => (
            <Fragment key={element.headerText}>
              <li className={styles.sidebarHeader}>{element.headerText}</li>
              {element.items.map((item) => {
                if (isSidebarItem(item)) {
                  return (
                    <li className={styles.sidebarItem} key={item.text}>
                      <Link
                        to={item.link || "#"}
                        className={styles.sidebarLink}
                      >
                        {item.icon && ICON_BY_TYPE[item.icon]}
                        {item.text}
                      </Link>
                    </li>
                  );
                }
                if (isSidebarDropdown(item)) {
                  const headerText =
                    typeof item.header === "string"
                      ? item.header
                      : item.header.text;
                  const dropdownPath = `${element.headerText}/${headerText}`;
                  const isActive = state.activeDropdownPaths.has(dropdownPath);
                  return (
                    <li className={styles.sidebarItem} key={headerText}>
                      <a
                        href="#"
                        className={`${styles.sidebarLink} ${
                          styles.hasDropdown
                        } ${isActive ? styles.active : ""}`}
                        onClick={() => {
                          setState((prevState) => {
                            const copiedSet = new Set([
                              ...prevState.activeDropdownPaths,
                            ]);
                            if (copiedSet.has(dropdownPath)) {
                              copiedSet.delete(dropdownPath);
                            } else {
                              copiedSet.add(dropdownPath);
                            }
                            return {
                              ...prevState,
                              activeDropdownPaths: copiedSet,
                            };
                          });
                        }}
                      >
                        {typeof item.header === "string" ? (
                          item.header
                        ) : (
                          <Fragment>
                            {item.header.icon && ICON_BY_TYPE[item.header.icon]}
                            {item.header.text}
                          </Fragment>
                        )}
                      </a>
                      <ul className={styles.sidebarDropdown}>
                        <li className={styles.sidebarItem}>
                          {item.items.map((dropdownItem) => (
                            <Link
                              to={dropdownItem.link || "#"}
                              className={styles.sidebarLink}
                              key={dropdownItem.text}
                            >
                              {dropdownItem.text}
                            </Link>
                          ))}
                        </li>
                      </ul>
                    </li>
                  );
                }
                console.error("Can't map item " + item);
                return <Fragment key={""} />;
              })}
            </Fragment>
          ))}
        </ul>
      </div>
    </aside>
  );
};
