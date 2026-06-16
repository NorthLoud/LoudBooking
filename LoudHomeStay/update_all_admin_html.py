import os
import re
import glob

TEMPLATE_HEAD = """<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Admin Dashboard</title>

    <!-- External Assets -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Outfit:wght@400;600;700;800&display=swap" rel="stylesheet" />
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" />
    <script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <style>
      :root {
        --primary: #4f46e5;
        --primary-light: #eef2ff;
        --secondary: #64748b;
        --success: #10b981;
        --warning: #f59e0b;
        --danger: #ef4444;
        --info: #0ea5e9;
        --bg-body: #f1f5f9;
        --sidebar-bg: #ffffff;
        --text-main: #0f172a;
        --text-muted: #64748b;
        --radius-lg: 16px;
        --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
      }

      * { margin: 0; padding: 0; box-sizing: border-box; font-family: "Inter", sans-serif; }
      h1, h2, h3, h4, .logo-text { font-family: "Outfit", sans-serif; }
      body { background-color: var(--bg-body); color: var(--text-main); display: flex; min-height: 100vh; overflow-x: hidden; }

      /* --- Sidebar --- */
      .sidebar { width: 260px; background: var(--sidebar-bg); border-right: 1px solid #e2e8f0; position: fixed; height: 100vh; z-index: 1000; padding: 30px 15px; }
      .logo-box { padding: 0 15px 40px; display: flex; align-items: center; gap: 12px; }
      .logo-icon { width: 40px; height: 40px; background: var(--primary); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: white; font-size: 20px; }
      .logo-text { font-size: 20px; font-weight: 800; color: var(--text-main); }
      .nav-group-label { font-size: 11px; font-weight: 700; text-transform: uppercase; color: var(--text-muted); letter-spacing: 1px; }
      .side-nav { list-style: none; }
      .side-nav a { display: flex; align-items: center; justify-content: space-between; padding: 12px 15px; border-radius: 10px; color: var(--secondary); text-decoration: none; font-weight: 500; font-size: 14px; transition: 0.2s; margin-bottom: 4px; }
      .side-nav a:hover, .side-nav a.active { background: var(--primary-light); color: var(--primary); }
      .side-nav a i:first-child { width: 24px; font-size: 18px; }
      .submenu { list-style: none; padding-left: 35px; margin-top: 4px; display: none; }
      .submenu.show { display: block; }

      /* --- Main Wrapper --- */
      .main-wrapper { flex: 1; margin-left: 260px; }
      .top-bar { height: 70px; background: white; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-between; padding: 0 40px; position: sticky; top: 0; z-index: 900; }
      .content-body { padding: 40px; }

      /* Table Card */
      .table-card { background: white; border-radius: var(--radius-lg); box-shadow: var(--shadow-md); overflow: hidden; border: 1px solid #e2e8f0; margin-top: 20px; }
      .table-header { padding: 20px 25px; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; background: #f8fafc; }

      .admin-table { width: 100%; border-collapse: collapse; }
      .admin-table th { background: #f8fafc; padding: 15px 25px; font-size: 11px; font-weight: 700; text-transform: uppercase; color: var(--text-muted); border-bottom: 1px solid #e2e8f0; text-align: left; cursor: pointer; }
      .admin-table td { padding: 15px 25px; font-size: 14px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }

      /* Form Styling */
      .form-group label { font-size: 12px; font-weight: 800; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px; display: block; }
      .form-control { border-radius: 12px; padding: 12px; border: 1.5px solid #e2e8f0; font-size: 14px; transition: 0.3s; width: 100%; }
      .form-control:focus { border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-light); outline: none; }

      /* Action Buttons */
      .btn-action { width: 35px; height: 35px; border-radius: 8px; border: 1px solid #e2e8f0; background: white; color: var(--secondary); display: inline-flex; align-items: center; justify-content: center; transition: 0.2s; cursor: pointer; margin-right: 5px; }
      .btn-action:hover { border-color: var(--primary); color: var(--primary); background: var(--primary-light); }
      .btn-action.text-danger:hover { border-color: var(--danger); color: var(--danger); background: #fef2f2; }
      .btn-luxury { padding: 10px 25px; border-radius: 12px; font-weight: 700; transition: 0.3s; }
      .badge-dot { width: 8px; height: 8px; background: var(--danger); border-radius: 50%; position: absolute; top: -2px; right: -2px; border: 2px solid white; }
    </style>
  </head>
  <body>
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="logo-box">
        <div class="logo-icon"><i class="fas fa-crown"></i></div>
        <div class="logo-text">Admin Dashboard</div>
      </div>
      <nav>
        <div class="nav-group-label">Hệ thống</div>
        <ul class="side-nav">
          <li><a href="dashboard.html"><i class="fas fa-chart-pie"></i> Bảng điều khiển</a></li>
          <li><a href="user.html"><i class="fas fa-users"></i> Quản lý tài khoản</a></li>
          <li><a href="hotel.html"><i class="fas fa-hotel"></i> Quản lý khách sạn</a></li>
          <li>
            <a href="javascript:void(0)" onclick="$('#roomSub').toggleClass('show')">
              <span><i class="fas fa-bed"></i> Quản lý Phòng</span>
              <i class="fas fa-chevron-down" style="font-size: 10px"></i>
            </a>
            <ul class="submenu" id="roomSub">
              <li><a href="roomtype.html">Loại phòng</a></li>
              <li><a href="room.html">Danh sách phòng</a></li>
            </ul>
          </li>
          <li>
            <a href="javascript:void(0)" onclick="$('#utilSub').toggleClass('show')">
              <span><i class="fas fa-concierge-bell"></i> Quản lý tiện ích</span>
              <i class="fas fa-chevron-down" style="font-size: 10px"></i>
            </a>
            <ul class="submenu" id="utilSub">
              <li><a href="utillity.html">Danh sách tiện ích</a></li>
              <li><a href="hotel-util.html">Tiện ích khách sạn</a></li>
              <li><a href="room-util.html">Tiện ích phòng</a></li>
            </ul>
          </li>
          <li><a href="review.html"><i class="fas fa-comment-alt"></i> Quản lý đánh giá</a></li>
          <li><a href="bill.html"><i class="fas fa-file-invoice-dollar"></i> Quản lý đơn đặt</a></li>
        </ul>
        <div class="nav-group-label">Tài khoản</div>
        <ul class="side-nav">
          <li><a href="javascript:void(0)" onclick="logout()" style="color: var(--danger)"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a></li>
        </ul>
      </nav>
    </aside>

    <!-- Main Content -->
    <div class="main-wrapper">
      <header class="top-bar">
        <div style="width: 400px; position: relative">
        </div>
        <div class="d-flex align-items-center gap-4">
          <div style="position: relative; cursor: pointer">
            <i class="fas fa-bell" style="font-size: 20px; color: var(--secondary)"></i>
            <span class="badge-dot"></span>
          </div>
          <div class="d-flex align-items-center gap-2" style="cursor: pointer">
            <div style="width: 35px; height: 35px; border-radius: 50%; background: var(--primary-light); color: var(--primary); display: flex; align-items: center; justify-content: center; font-weight: 800;">A</div>
            <span style="font-weight: 600; font-size: 14px">Admin</span>
          </div>
        </div>
      </header>

      <div class="content-body">
"""

TEMPLATE_FOOT = """
      </div>
    </div>
  </body>
</html>
"""

def replace_attributes(html_str):
    # Thay thẻ class cho table
    html_str = re.sub(r'class="table[^"]*"', 'class="admin-table"', html_str)
    
    # Đóng gói table-responsive vào table-card
    html_str = html_str.replace('<div class="table-responsive">', 
        '<div class="table-card">\n<div class="table-header"><h5 style="margin: 0; font-weight: 700;">Danh sách dữ liệu</h5></div>\n<div class="table-responsive">')
    
    # Pagination
    html_str = html_str.replace('</ul>\n            </div>', '</ul>\n            </div>\n</div>')
    
    # Nút bấm
    html_str = html_str.replace('btn-primary', 'btn-primary btn-luxury')
    html_str = html_str.replace('btn-success', 'btn-success btn-luxury')
    
    # Modal (cách ly style modal nếu có)
    html_str = html_str.replace('class="modal-content"', 'class="modal-content" style="border-radius: 16px; border: none; box-shadow: 0 10px 30px rgba(0,0,0,0.1);"')
    html_str = html_str.replace('class="modal-header"', 'class="modal-header" style="border-bottom: 1px solid #e2e8f0; background: #f8fafc; border-radius: 16px 16px 0 0;"')
    html_str = html_str.replace('class="modal-footer"', 'class="modal-footer" style="border-top: 1px solid #e2e8f0;"')
    html_str = html_str.replace('class="modal-title"', 'class="modal-title" style="font-weight: 800;"')

    # Thay title thành dạng mới
    # Chuyển các thẻ <h1 class="mb-4 text-center"> ... </h1> thành page-header
    # Hoặc xoá <div class="container mt-4">
    return html_str

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Regex: lấy phần trong <section>
    match_section = re.search(r'<section.*?>(.*?)</section>', content, re.DOTALL)
    if not match_section:
        return # Skip

    section_content = match_section.group(1)

    # Tìm phần Modal (nếu có, modal có thể nằm ngoài section, bắt đầu bằng div modal fade)
    modals = []
    modal_matches = re.finditer(r'<div class="modal fade"[^>]*>.*?</div>\s*</div>\s*</div>', content, re.DOTALL)
    for m in modal_matches:
        modals.append(m.group(0))

    # Tìm script tags (ngoại trừ các script sidebar)
    scripts = []
    script_matches = re.finditer(r'<script>(.*?)</script>', content, re.DOTALL)
    for m in script_matches:
        s_code = m.group(1)
        if "sidebarToggle" not in s_code and "chart" not in s_code:
            scripts.append(f"<script>{s_code}</script>")

    # Transform inner content
    # Remove container mt-4 div
    section_content = re.sub(r'<div class="container mt-4">\s*', '', section_content, count=1)
    # Remove the last closing div corresponding to container
    section_content = section_content.rstrip()
    if section_content.endswith('</div>'):
        section_content = section_content[:-6]

    # Transform h1
    h1_match = re.search(r'<h1[^>]*>.*?<b>(.*?)</b>.*?</h1>', section_content, re.DOTALL)
    if not h1_match:
        h1_match = re.search(r'<h1[^>]*>(.*?)</h1>', section_content, re.DOTALL)
        
    title = h1_match.group(1).strip() if h1_match else "Quản lý"
    
    # Remove old h1
    section_content = re.sub(r'<h1[^>]*>.*?</h1>', '', section_content, count=1, flags=re.DOTALL)

    new_header = f"""
        <div class="page-header">
          <div>
            <h2>{title}</h2>
            <p style="color: var(--text-muted)">Quản lý {title.lower()} trong hệ thống</p>
          </div>
        </div>
    """

    section_content = new_header + section_content
    section_content = replace_attributes(section_content)

    for i in range(len(modals)):
        modals[i] = replace_attributes(modals[i])

    script_str = "\n".join(scripts)
    # Cập nhật style nút bấm trong script
    script_str = re.sub(r'<button class="btn btn-sm btn-outline-primary[^"]*"', '<button class="btn-action"', script_str)
    script_str = re.sub(r'<button class="btn btn-sm btn-outline-danger[^"]*"', '<button class="btn-action text-danger"', script_str)
    script_str = re.sub(r'<button class="btn btn-sm btn-outline-info[^"]*"', '<button class="btn-action text-info"', script_str)

    # Nếu file là dashboard.html hay utillity.html, bỏ qua
    final_content = TEMPLATE_HEAD + section_content + "\n" + "\n".join(modals) + "\n" + script_str + TEMPLATE_FOOT

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(final_content)

html_files = glob.glob('d:/LoudHomeStay/src/main/resources/static/admin/html/*.html')
for f in html_files:
    if f.endswith('utillity.html') or f.endswith('dashboard.html'):
        continue
    print(f"Processing {f}...")
    process_file(f)

print("Done all admin files")
