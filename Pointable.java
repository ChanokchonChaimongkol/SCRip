package YummyList.Interface;

/**
 * ========================================================
 *  Pointable — Interface สำหรับระบบแต้มสะสม (Points)
 * ========================================================
 * กำหนด "สัญญา" ว่า class ใดก็ตามที่ implements Pointable
 * จะต้องมีความสามารถ ดึงแต้ม และ เพิ่มแต้ม
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← Member.java  implements Pointable
 *                  (Member คือผู้ถือแต้มสะสมในระบบ)
 *
 * เหตุผลที่ใช้ Interface:
 *   ถ้าในอนาคตมี class อื่น (เช่น VIPMember, GuestPass)
 *   ก็สามารถ implements Pointable ได้ทันที
 *   โดยไม่ต้องแก้ code ที่เรียกใช้ผ่าน interface นี้
 */
public interface Pointable {

    /**
     * getPoints() — คืนค่าแต้มสะสมปัจจุบัน
     *
     * ใช้ใน:
     *   - MemberFrame   → แสดง "⭐ X pts" บน top bar และ summary card
     *   - ReceiptFrame  → แสดง "Total Points: ⭐ X pts" บนใบเสร็จ
     *   - MemberFrame.doCheckout() → ตรวจสอบว่ามีแต้มพอใช้หรือไม่
     *
     * @return จำนวนแต้มคงเหลือ (int, ไม่ติดลบ ถ้าระบบทำงานถูกต้อง)
     */
    int getPoints();

    /**
     * addPoints(int amount) — เพิ่ม (หรือลด) แต้มสะสม
     *
     * ใช้ใน:
     *   - MemberFrame.doCheckout():
     *       member.addPoints(-usePoint)  → หักแต้มที่ใช้จ่าย (ส่ง amount ติดลบ)
     *       member.addPoints(earned)     → บวกแต้มที่ได้รับ 5% ของยอดสุทธิ
     *
     * @param amount จำนวนแต้มที่จะเพิ่ม (ถ้าเป็นลบ = หักแต้ม)
     */
    void addPoints(int amount);
}
