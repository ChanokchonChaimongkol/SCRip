package YummyList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * ========================================================
 *  AdminFrame — หน้าจัดการเมนูอาหารสำหรับ Admin
 * ========================================================
 * extends JFrame → เป็นหน้าต่างอิสระ
 * Admin สามารถ Add / Edit / Delete เมนูได้ผ่าน form ทางขวา
 * ตารางทางซ้ายแสดงเมนูทั้งหมด (อ่านจาก MenuManager)
 *
 * Layout หลัก (BorderLayout):
 *   NORTH  → top bar สีแดง (Admin Panel + ปุ่ม Logout)
 *   CENTER → ตารางเมนูทั้งหมด
 *   EAST   → form เพิ่ม/แก้ไขเมนู + ปุ่ม CRUD
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← สร้างโดย  LoginFrame.doAdminLogin()  → new AdminFrame(menuManager, memberManager)
 *   → เรียก      MenuManager (addMenu, editMenu, deleteMenu, getAllMenus, getMenuById)
 *   → เปิด       LoginFrame  เมื่อ Logout
 *   → ถือ        MemberManager เพื่อส่งต่อเวลา logout
 */
public class AdminFrame extends JFrame {

    /** menuManager — ใช้สำหรับ CRUD เมนู และแสดงตาราง */
    private final MenuManager menuManager;

    /** memberManager — ไม่ได้ใช้โดยตรงใน AdminFrame แต่ส่งต่อตอน logout */
    private final MemberManager memberManager;

    /**
     * tableModel — DefaultTableModel เชื่อมกับ JTable (menuTable)
     *
     * DefaultTableModel:
     *   เก็บข้อมูลตาราง (rows × columns) ใน memory
     *   ใช้ setRowCount(0) เพื่อล้างข้อมูลทั้งหมดก่อน refresh
     *   addRow(Object[]) เพิ่มแถวใหม่
     *   isCellEditable override → return false (ห้าม edit ใน cell)
     */
    private DefaultTableModel tableModel;

    /**
     * menuTable — JTable แสดงรายการเมนูทั้งหมด
     *
     * เชื่อมกับ tableModel
     * ListSelectionListener → เมื่อเลือกแถว → loadSelectedToForm()
     */
    private JTable menuTable;

    // ==================== Form Fields (EAST panel) ====================
    /** idField    — JTextField สำหรับกรอก Menu ID */
    private JTextField idField;

    /** nameField  — JTextField สำหรับกรอกชื่อเมนู */
    private JTextField nameField;

    /** priceField — JTextField สำหรับกรอกราคา */
    private JTextField priceField;

    /** timeField  — JTextField สำหรับกรอกเวลาทำอาหาร */
    private JTextField timeField;

    /**
     * formTitle — JLabel หัวข้อ form
     * เปลี่ยนระหว่าง "Add New Menu" (ไม่ได้เลือกแถว)
     *             กับ "Edit Menu"    (เลือกแถวแล้ว)
     */
    private JLabel formTitle;

    /**
     * Constructor — สร้างหน้าต่าง Admin พร้อม UI ทั้งหมด
     *
     * ลำดับการสร้าง UI:
     *   1. ตั้งค่า JFrame
     *   2. buildTopBar()    → NORTH
     *   3. buildTablePanel() → CENTER
     *   4. buildFormPanel()  → EAST
     *   5. setVisible(true)
     *
     * @param men  MenuManager สำหรับ CRUD เมนู
     * @param mm   MemberManager สำหรับส่งต่อตอน logout
     */
    public AdminFrame(MenuManager men, MemberManager mm) {
        menuManager   = men;
        memberManager = mm;
        setTitle("YummyList — Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 580);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UI.BACKGROUND);

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(),  BorderLayout.EAST);
        setVisible(true);
    }

    /**
     * buildTopBar() — สร้าง top bar สีแดงพร้อมปุ่ม Logout
     *
     * ใช้ UI.topBar() เป็นฐาน แต่เปลี่ยนสี background เป็น UI.DANGER (แดง)
     * เพื่อบอกว่าอยู่ใน Admin mode
     *
     * ปุ่ม Logout:
     *   - background ขาวโปร่งแสง (Color(255,255,255,60))
     *   - hover สว่างขึ้น (Color(255,255,255,100))
     *   - ActionListener: dispose() → new LoginFrame(...)
     *
     * @return JPanel top bar สีแดง
     */
    private JPanel buildTopBar() {
        // ปุ่ม Logout สีขาวโปร่งแสง
        UI.Btn logout = new UI.Btn("Logout", new Color(255,255,255,60), new Color(255,255,255,100));
        logout.setForeground(Color.WHITE);
        logout.setPreferredSize(new Dimension(90, 32));
        logout.addActionListener(e -> {
            dispose();   // ปิด AdminFrame
            new LoginFrame(memberManager, menuManager);   // กลับหน้า Login
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(logout);

        JPanel bar = UI.topBar("🔧  Admin Panel", right);
        bar.setBackground(UI.DANGER);   // เปลี่ยนเป็นสีแดง (override จาก PRIMARY)
        return bar;
    }

    /**
     * buildTablePanel() — สร้าง panel ตารางเมนูทั้งหมด (CENTER)
     *
     * ประกอบด้วย:
     *   - UI.title("All Menus") → หัวข้อ NORTH ของ panel นี้
     *   - JTable + DefaultTableModel:
     *       columns: "ID", "Name", "Price (฿)", "Cook Time (min)"
     *       isCellEditable override → false (ห้าม double-click edit)
     *   - UI.styleTable() → ตกแต่ง style ตาราง
     *   - ListSelectionListener:
     *       เมื่อเลือกแถว → loadSelectedToForm() โหลดข้อมูลลง form ขวา
     *   - Column width ปรับ: ID=40, Name=180 (ป้องกัน column แคบเกิน)
     *   - refreshTable() → โหลดข้อมูลเริ่มต้น
     *
     * @return JPanel ตาราง
     */
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UI.BACKGROUND);
        p.setBorder(new EmptyBorder(16, 16, 16, 8));
        p.add(UI.title("All Menus"), BorderLayout.NORTH);

        // DefaultTableModel: กำหนด column headers, override isCellEditable
        tableModel = new DefaultTableModel(
                new String[]{"ID","Name","Price (฿)","Cook Time (min)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        menuTable = new JTable(tableModel);
        UI.styleTable(menuTable);   // ใช้ style จาก UI class
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);   // เลือกได้ครั้งละ 1 แถว

        // ListSelectionListener — เมื่อ selection เปลี่ยน → โหลดข้อมูลลง form
        menuTable.getSelectionModel().addListSelectionListener(e -> loadSelectedToForm());

        // ปรับความกว้าง column
        menuTable.getColumnModel().getColumn(0).setPreferredWidth(40);    // ID
        menuTable.getColumnModel().getColumn(1).setPreferredWidth(180);   // Name

        refreshTable();   // โหลดข้อมูลเมนูทั้งหมดครั้งแรก
        p.add(UI.scroll(menuTable), BorderLayout.CENTER);
        return p;
    }

    /**
     * buildFormPanel() — สร้าง form เพิ่ม/แก้ไขเมนู (EAST)
     *
     * ใช้ UI.Card ครอบ GridBagLayout
     * ประกอบด้วยตามลำดับ:
     *   formTitle (JLabel) → เปลี่ยน text ตาม context
     *   Label + Field แต่ละคู่: ID, Name, Price, Cook Time
     *   ปุ่ม 4 ปุ่ม: Add Menu, Save Edit, Delete, Clear
     *
     * GridBagConstraints:
     *   c.fill = HORIZONTAL → ขยายเต็มความกว้าง
     *   c.weightx = 1       → ใช้ความกว้างเต็ม column
     *   c.gridx = 0         → column เดียว
     *   c.gridy = i         → แถวตาม index
     *
     * Object[] rows — array ผสม JLabel และ Component
     *   rows[i] instanceof String → สร้าง UI.label()
     *   rows[i] เป็น Component   → add ตรงๆ
     *
     * @return JPanel form ขนาด 260px ทางขวา
     */
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UI.BACKGROUND);
        outer.setBorder(new EmptyBorder(16, 8, 16, 16));
        outer.setPreferredSize(new Dimension(260, 0));   // ความกว้างคงที่ 260px

        UI.Card card = new UI.Card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.insets = new Insets(5, 4, 5, 4);

        formTitle = new JLabel("Add New Menu");
        formTitle.setFont(UI.F_TITLE);
        formTitle.setForeground(UI.PRIMARY);

        // สร้าง input fields
        idField    = UI.field();
        nameField  = UI.field();
        priceField = UI.field();
        timeField  = UI.field();

        // rows array: สลับ String label กับ Component
        Object[] rows = {
            formTitle,
            "Menu ID",         idField,
            "Name",            nameField,
            "Price (฿)",       priceField,
            "Cook Time (min)", timeField
        };

        // วนเพิ่ม component ลง GridBag
        for (int i = 0; i < rows.length; i++) {
            c.gridy = i;
            c.insets = new Insets(i == 0 ? 0 : 5, 4, 5, 4);   // formTitle ไม่มี top insets
            card.add(rows[i] instanceof String
                    ? UI.label((String) rows[i])         // String → label
                    : (java.awt.Component) rows[i],       // Component → ใส่ตรงๆ
                    c);
        }

        // สร้างปุ่ม CRUD
        UI.Btn btnAdd  = UI.btnPrimary("Add Menu");    // ส้ม
        UI.Btn btnEdit = UI.btnSuccess("Save Edit");   // เขียว
        UI.Btn btnDel  = UI.btnDanger("Delete");       // แดง
        UI.Btn btnClr  = UI.btnGray("Clear");          // เทา

        // ตั้งความสูงปุ่มทุกตัว
        for (UI.Btn b : new UI.Btn[]{btnAdd, btnEdit, btnDel, btnClr})
            b.setPreferredSize(new Dimension(0, UI.BTN_H));

        // ActionListener แต่ละปุ่ม
        btnAdd .addActionListener(e -> doAdd());     // เพิ่มเมนูใหม่
        btnEdit.addActionListener(e -> doEdit());    // บันทึกการแก้ไข
        btnDel .addActionListener(e -> doDelete());  // ลบเมนู
        btnClr .addActionListener(e -> clearForm()); // ล้าง form

        // วางปุ่มที่ gridy 9-12 (ต่อจาก rows)
        int[] btnGridY     = {9, 10, 11, 12};
        UI.Btn[] btns      = {btnAdd, btnEdit, btnDel, btnClr};
        for (int i = 0; i < btns.length; i++) {
            c.gridy = btnGridY[i];
            c.insets = new Insets(i == 0 ? 8 : 4, 4, 4, 4);   // ปุ่มแรก margin top 8
            card.add(btns[i], c);
        }

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ==================== Logic Methods ====================

    /**
     * refreshTable() — โหลดข้อมูลเมนูทั้งหมดลง JTable ใหม่
     *
     * 1. setRowCount(0) → ล้างแถวทั้งหมดก่อน
     * 2. menuManager.getAllMenus().forEach() → วนทุก Menu
     * 3. addRow(Object[]) → เพิ่มแต่ละ Menu เป็น 1 แถว
     *    String.format("%.2f", price) → แสดงราคา 2 ทศนิยม
     *
     * เรียกจาก: constructor, doAdd(), doEdit(), doDelete(), clearForm()
     */
    private void refreshTable() {
        tableModel.setRowCount(0);   // ล้างแถวทั้งหมด
        menuManager.getAllMenus().forEach(m -> tableModel.addRow(new Object[]{
                m.getId(),
                m.getName(),
                String.format("%.2f", m.getPrice()),   // format ราคา 2 ทศนิยม
                m.getCookTime()
        }));
    }

    /**
     * loadSelectedToForm() — โหลดข้อมูลแถวที่เลือกลง form fields
     *
     * เรียกจาก ListSelectionListener เมื่อ selection เปลี่ยน
     *
     * 1. menuTable.getSelectedRow() → index แถวที่เลือก (-1 ถ้าไม่มี)
     * 2. tableModel.getValueAt(row, 0) → ดึง ID จาก cell
     * 3. menuManager.getMenuById(id) → ได้ Menu object
     * 4. set text ทุก field + เปลี่ยน formTitle เป็น "Edit Menu"
     */
    private void loadSelectedToForm() {
        int row = menuTable.getSelectedRow();
        if (row < 0) return;   // ไม่มีการเลือก → ออก

        int id = (Integer) tableModel.getValueAt(row, 0);   // ดึง ID จาก column 0
        Menu m = menuManager.getMenuById(id);
        if (m == null) return;

        // เติมข้อมูลลง form fields
        idField.setText(String.valueOf(m.getId()));
        nameField.setText(m.getName());
        priceField.setText(String.valueOf(m.getPrice()));
        timeField.setText(String.valueOf(m.getCookTime()));
        formTitle.setText("Edit Menu");   // เปลี่ยน title ให้รู้ว่ากำลัง edit
    }

    /**
     * doAdd() — เพิ่มเมนูใหม่จากข้อมูลใน form
     *
     * 1. อ่านค่าจาก fields พร้อม parse int/double
     *    NumberFormatException → แจ้ง error ถ้า parse ไม่ได้
     * 2. สร้าง new Menu(id, name, price, time)
     * 3. menuManager.addMenu(menu):
     *    true  → success + refreshTable() + clearForm()
     *    false → error "ID already exists."
     */
    private void doAdd() {
        try {
            boolean ok = menuManager.addMenu(new Menu(
                    Integer.parseInt(idField.getText().trim()),      // parse id
                    nameField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),  // parse price
                    Integer.parseInt(timeField.getText().trim())      // parse cookTime
            ));
            if (ok) { success("Menu added!"); refreshTable(); clearForm(); }
            else      error("ID already exists.");
        } catch (NumberFormatException e) {
            error("Please enter valid numbers.");
        }
    }

    /**
     * doEdit() — บันทึกการแก้ไขเมนูที่เลือก
     *
     * 1. parse id จาก idField
     * 2. menuManager.editMenu(id, name, price, time):
     *    true  → success + refreshTable() + clearForm()
     *    false → error "Menu ID X not found."
     * 3. NumberFormatException → แจ้ง error
     */
    private void doEdit() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            boolean ok = menuManager.editMenu(
                    id,
                    nameField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    Integer.parseInt(timeField.getText().trim())
            );
            if (ok) { success("Menu updated!"); refreshTable(); clearForm(); }
            else      error("Menu ID " + id + " not found.");
        } catch (NumberFormatException e) {
            error("Please enter valid numbers.");
        }
    }

    /**
     * doDelete() — ลบเมนูที่ระบุ ID
     *
     * 1. parse id จาก idField
     * 2. ถาม confirm dialog ก่อนลบ (YES/NO)
     *    ถ้า YES:
     *      menuManager.deleteMenu(id):
     *        true  → success + refreshTable() + clearForm()
     *        false → error "Menu ID X not found."
     * 3. NumberFormatException → error "Please select or enter a Menu ID."
     */
    private void doDelete() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            // confirm dialog ก่อนลบ
            if (JOptionPane.showConfirmDialog(this,
                    "Delete menu ID " + id + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (menuManager.deleteMenu(id)) {
                    success("Menu deleted!"); refreshTable(); clearForm();
                } else {
                    error("Menu ID " + id + " not found.");
                }
            }
        } catch (NumberFormatException e) {
            error("Please select or enter a Menu ID.");
        }
    }

    /**
     * clearForm() — ล้างข้อมูลทุก field และ reset selection
     *
     * setText("") → ล้าง text ทุก field
     * menuTable.clearSelection() → ยกเลิก selection ในตาราง
     * formTitle.setText("Add New Menu") → กลับ title เป็น default
     *
     * เรียกจาก: doAdd(), doEdit(), doDelete(), ปุ่ม "Clear"
     */
    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        priceField.setText("");
        timeField.setText("");
        menuTable.clearSelection();        // ยกเลิก highlight ในตาราง
        formTitle.setText("Add New Menu"); // reset title
    }

    /**
     * success(String msg) — แสดง dialog แจ้งสำเร็จ
     * JOptionPane.INFORMATION_MESSAGE = ไอคอน ℹ
     */
    private void success(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * error(String msg) — แสดง dialog แจ้ง error
     * JOptionPane.ERROR_MESSAGE = ไอคอน ✖
     */
    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
