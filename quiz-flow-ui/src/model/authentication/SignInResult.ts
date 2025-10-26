export interface SignInResult {
  accessTokenId: string;
  refreshTokenId: string;
  accessToken: string;
  authorizationScopes: string[];
  accessTokenExpirationDate: Date;
  refreshTokenExpirationDate: Date;
}
