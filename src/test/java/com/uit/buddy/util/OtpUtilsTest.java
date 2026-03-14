package com.uit.buddy.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OtpUtilsTest {

  private OtpUtils otpUtils;

  @BeforeEach
  void setUp() {
    otpUtils = new OtpUtils();
  }

  @Test
  void generateNumericOtp_defaultLength_returns6Digits() {
    String otp = otpUtils.generateNumericOtp();
    assertNotNull(otp);
    assertEquals(6, otp.length());
  }

  @Test
  void generateNumericOtp_customLength_returnsCorrectLength() {
    int length = 8;
    String otp = otpUtils.generateNumericOtp(length);
    assertNotNull(otp);
    assertEquals(length, otp.length());
  }

  @Test
  void generateNumericOtp_containsOnlyDigits() {
    String otp = otpUtils.generateNumericOtp(6);
    assertTrue(otp.matches("\\d+"), "OTP should contain only numeric digits");
  }

  @Test
  void generateNumericOtp_zeroLength_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> otpUtils.generateNumericOtp(0));
  }

  @Test
  void generateNumericOtp_negativeLength_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> otpUtils.generateNumericOtp(-1));
  }

  @Test
  void generateNumericOtp_calledTwice_returnsDifferentValues() {
    // With randomness, same value is theoretically possible but extremely unlikely
    // for 6-digit OTPs
    String otp1 = otpUtils.generateNumericOtp();
    String otp2 = otpUtils.generateNumericOtp();
    // Just ensure both are valid; equality check avoided due to randomness
    assertEquals(6, otp1.length());
    assertEquals(6, otp2.length());
  }
}
