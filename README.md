##  🚩 [ISS Affiliated]Legendary Mage  v.1.0.1
###  ❌ Error fixes:
* Fixed damage calculation errors in element translation and added EMIffect compatibility to BUFF **Ender Echo**
###  🛠️ Add and repair
* **Implosion**  Modification: Now the burst spell will continuously gather enemies during the singing period
* **Blizzard**  Modification: Blizzard now requires selecting a target for release
* **Element response**: Allow the use of JSON to configure compatibility with other spell genres
* New Spells:  **Ice Explosion Cone**/**Focused Ice Cone**/**Giant Snowball**(There are issues, still under development)
## 🚩 This version contains errors. I will not save or submit a version with errors. I am keeping it for future reference in case of related error troubleshooting

### ❗Issues and reasons with this version:

* **Living Ice Sculpture**: Unable to correctly set the health of summoned creatures
  **Reason**: After the correction of spell power calculation in **V.1.0.1**, the blood volume calculation for ice sculptures was not updated, resulting in the erroneous generation of ice sculptures with a blood volume of 0
