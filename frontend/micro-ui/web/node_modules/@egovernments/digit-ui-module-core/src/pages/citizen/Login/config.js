export const loginSteps = [
  {
    texts: {
      header: "PROVIDE MOBILE NUMBER",
      cardText: "LOGIN",
      nextText: "CS_COMMONS_NEXT",
      submitBarLabel: "NEXT",
    },
    inputs: [
      {
        label: "MOBILE NUMBER",
        type: "text",
        name: "mobileNumber",
        error: "ERR_HRMS_INVALID_MOB_NO",
        validation: {
          required: true,
          minLength: 10,
          maxLength: 10,
        },
      },
    ],
  },
  {
    texts: {
      header: "LOGIN OTP",
      cardText: "ENTER OTP",
      nextText: "CS_COMMONS_NEXT",
      submitBarLabel: "NEXT",
    },
  },
  {
    texts: {
      header: "CS_LOGIN_PROVIDE_NAME",
      cardText: "CS_LOGIN_NAME_TEXT",
      nextText: "CS_COMMONS_NEXT",
      submitBarLabel: "CS_COMMONS_NEXT",
    },
    inputs: [
      {
        label: "CORE_COMMON_NAME",
        type: "text",
        name: "name",
        error: "CORE_COMMON_NAME_VALIDMSG",
        validation: {
          required: true,
          minLength: 1,
          pattern: /^[^{0-9}^\$\"<>?\\\\~!@#$%^()+={}\[\]*,/_:;“”‘’]{1,50}$/i
        },
      },
    ],
  },
];
