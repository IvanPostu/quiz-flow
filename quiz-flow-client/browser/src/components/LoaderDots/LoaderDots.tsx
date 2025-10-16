import * as styles from './styles.module.scss';

export const LoaderDots = () => {
  return (
    <div className={styles.loader}>
      <span className={styles.dot}></span>
      <span className={styles.dot}></span>
      <span className={styles.dot}></span>
    </div>
  )
}
