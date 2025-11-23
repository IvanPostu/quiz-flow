// TODO: to cover with unit tests
export function randomArrayElements<T>(
  array: T[],
  randomItemsCount: number
): T[] {
  if (randomItemsCount >= array.length) {
    return [...array];
  }
  if (randomItemsCount === 0) {
    return [];
  }

  const result: T[] = [];
  const indexes = array.map((_, index) => index);

  for (let i = 0; i < randomItemsCount; i++) {
    const randomIndex = Math.floor(Math.random() * indexes.length);
    const randomElement = array[indexes[randomIndex]];
    indexes.splice(randomIndex, 1);

    result.push(randomElement);
  }

  return result;
}
