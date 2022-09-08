package sysmo.reform.shared.examples

object BioReactor {
  /**
   * Device:
   *   - name: node1
   *   - type: ESP32
   *   - sampling_rate: 1 ms
   *   - control_rate: 1ms
   *
   * Parameters:
   *   - t_target, dt
   *   - t_mixer_on, t_mixer_off
   *   - gas_cyl_min, gas_cyl_max
   *   - ph_min, ph_max, ph_target
   *
   *
   * Input signals
   *   - reactor_t: Reactor Temperature (ds18b20 over 1 wire)
   *   - reactor_p: Reactor Pressure (?)
   *   - reactor_ph: pH (analog)
   *   - gas_cyl_pos: Gas cylinder position (analog)
   *   - Camera (?)
   *   - rt_clock: Real time clock (integer over I2C?)
   *
   * Output signals
   *   - heater_on: Heater (On/Off)
   *   - mixer_rate: Mixer (PWM)
   *   - acid_on: Acid (On/Off)
   *   - base_on (On/Off)
   *   - gas_release: Gas release valve (On/Off)
   *
   * Control
   *  - Temperature regulator
   *    - name: TR
   *    - type: Hysteresis
   *    - setpoint: t_target
   *    - delta: dt
   *    - input: reactor_t
   *    - active_on_high: false
   *    - output: heater_on
   *
   *  - Mixer regulator:
   *    - name: MR
   *    - type: Duty cycle
   *    - period: (t_mixer_on + t_mixer_off)
   *    - duty: t_mixer_on / (t_mixer_on + t_mixer_off)
   *
   *  - Gas cylinder regulator
   *    - name: GSR
   *    - type: HysteresisMinMax
   *    - min: gas_cyl_min
   *    - max: gas_cyl_max
   *    - active_on_high: true
   *    - output: gas_release
   *
   *  - PH regulator high
   *    - name: PHB
   *    - type: HysteresisMinMax
   *    - min: ph_target
   *    - max: ph_max
   *    - active_on_high: true
   *    - output: acid_on
   *
   *  - PH regulator low
   *    - name: PHB
   *    - type: HysteresisMinMax
   *    - min: ph_min
   *    - max: ph_target
   *    - active_on_high: false
   *    - output: base_on
   *
   * Devices
   *   - Central server (RPI)
   *   - Controller (ESP32)
   *
   *
   *
   */
}
