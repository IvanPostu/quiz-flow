import { IoCheckboxOutline, IoSquareOutline } from "react-icons/io5";
import * as styles from "./styles.module.scss";

interface CheckboxPropsType {
  checked: boolean;
  label: string;
  onChange?: (isChecked: boolean) => void;
}

export function Checkbox({ checked, onChange, label }: CheckboxPropsType) {
  return (
    <label className={styles.checkboxWrapper}>
      <input
        type="checkbox"
        checked={checked}
        onChange={(e) => onChange && onChange(e.target.checked)}
        className={styles.hiddenInput}
      />

      <span className={styles.customBox}>
        {checked ? <IoCheckboxOutline /> : <IoSquareOutline />}
      </span>

      {label && <span className={styles.label}>{label}</span>}
    </label>
  );
}
