import { Amplify } from 'aws-amplify';

Amplify.configure({
  Auth: {
    Cognito: {
      userPoolId: 'us-east-1_xgz8uSxKQ',
      userPoolClientId: '2ju9qhemekdnqg14c7m5r9ge3p',
      signUpVerificationMethod: 'code',
    }
  }
});