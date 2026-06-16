import re

def sync_file(manager_path, admin_path):
    with open('d:/LoudHomeStay/src/main/resources/static/admin/html/room.html', 'r', encoding='utf-8') as f:
        admin_room = f.read()
    
    match = re.search(r'(<aside class="sidebar">.*?</aside>)', admin_room, re.DOTALL)
    if not match:
        print("Failed to find admin sidebar")
        return
    admin_sidebar = match.group(1)
    
    with open(manager_path, 'r', encoding='utf-8') as f2:
        manager_html = f2.read()
    
    # 1. Replace sidebar
    manager_html = re.sub(r'<aside class="sidebar">.*?</aside>', admin_sidebar, manager_html, flags=re.DOTALL)
    
    # 2. Replace /my endpoints
    manager_html = manager_html.replace(' + "/my"', '')
    
    # 3. Fix Top Bar Avatar
    manager_html = re.sub(
        r'<div[^>]*>\s*M\s*</div>\s*<span[^>]*>Quản lý viên</span>',
        '<div style="width: 35px; height: 35px; border-radius: 50%; background: var(--primary-light); color: var(--primary); display: flex; align-items: center; justify-content: center; font-weight: 800;">A</div><span style="font-weight: 600; font-size: 14px">Admin</span>',
        manager_html,
        flags=re.DOTALL
    )
    
    with open(admin_path, 'w', encoding='utf-8') as out:
        out.write(manager_html)
    print(f'Synced {admin_path} from {manager_path}')

# Sync roomtype.html
sync_file(
    'd:/LoudHomeStay/src/main/resources/static/manager/html/roomtype.html',
    'd:/LoudHomeStay/src/main/resources/static/admin/html/roomtype.html'
)

# Sync room-util.html
sync_file(
    'd:/LoudHomeStay/src/main/resources/static/manager/html/room-util.html',
    'd:/LoudHomeStay/src/main/resources/static/admin/html/room-util.html'
)

# Sync hotel-util.html
sync_file(
    'd:/LoudHomeStay/src/main/resources/static/manager/html/hotel-util.html',
    'd:/LoudHomeStay/src/main/resources/static/admin/html/hotel-util.html'
)

print("All done!")
