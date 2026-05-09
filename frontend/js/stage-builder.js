let canvas;
let currentTool = 'select';
let clipboard = null;
let undoStack = [];
let isRedoing = false;

// Drawing state variables
let isDrawingShape = false;
let startX, startY;
let activeShape = null;

// Polygon state variables
let polygonPoints = [];
let polygonLines = [];
let activeLine = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('stageCanvas')) return;
    
    // Init Fabric Canvas
    canvas = new fabric.Canvas('stageCanvas', {
        selection: true,
        preserveObjectStacking: true,
        backgroundColor: '#111520'
    });

    const wrapper = document.getElementById('canvasScrollArea');
    if (wrapper) {
        canvas.setWidth(Math.max(1000, wrapper.clientWidth));
        canvas.setHeight(Math.max(800, wrapper.clientHeight));
    }

    drawGrid();
    setupEvents();
    setupKeyboardShortcuts();
    
    // Khởi tạo state gốc
    setTimeout(() => saveState(), 100);
});

function drawGrid() {
    canvas.clear();
    const grid = 50;
    const width = canvas.width;
    const height = canvas.height;
    
    for (let i = 0; i < (width / grid); i++) {
        canvas.add(new fabric.Line([ i * grid, 0, i * grid, height], { stroke: '#ffffff11', selectable: false, evented: false, strokeWidth: 1 }));
    }
    for (let i = 0; i < (height / grid); i++) {
        canvas.add(new fabric.Line([ 0, i * grid, width, i * grid], { stroke: '#ffffff11', selectable: false, evented: false, strokeWidth: 1 }));
    }
}

function toggleStageBuilder() {
    const isEnabled = document.getElementById('enableStageBuilder').checked;
    const container = document.getElementById('stageBuilderContainer');
    container.style.display = isEnabled ? 'block' : 'none';
    if (isEnabled && canvas) {
        canvas.calcOffset();
        updateTicketTypeDropdown();
    }
}

function updateTicketTypeDropdown() {
    const select = document.getElementById('sbObjTicketType');
    const currentValue = select.value;
    select.innerHTML = '<option value="">-- Chọn Khu Vực --</option>';
    
    // Quét tất cả các hạng vé
    document.querySelectorAll('#hangVeContainer .dynamic-box').forEach(hvItem => {
        const hvName = hvItem.querySelector('.hv-name')?.value.trim() || 'Hạng vé chưa tên';
        
        // Quét các khu vực bên trong hạng vé đó
        hvItem.querySelectorAll('.kv-item').forEach(kvItem => {
            const kvNameInput = kvItem.querySelector('.kv-name');
            const kvColorInput = kvItem.querySelector('.kv-color');
            const maKhuVuc = kvItem.dataset.maKhuVuc;
            
            if (kvNameInput) {
                let kvName = kvNameInput.value.trim() || 'Khu vực chưa tên';
                let kvColor = kvColorInput ? kvColorInput.value : '#3B82F6';
                let id = maKhuVuc ? maKhuVuc : kvItem.id;
                
                let option = document.createElement('option');
                option.value = id;
                option.dataset.color = kvColor;
                option.textContent = `${kvName} (${hvName})`;
                select.appendChild(option);
            }
        });
    });

    if (currentValue) {
        select.value = currentValue;
    }
}

document.querySelectorAll('.sb-tool').forEach(btn => {
    btn.addEventListener('click', (e) => {
        document.querySelectorAll('.sb-tool').forEach(b => b.classList.remove('active'));
        const targetBtn = e.currentTarget;
        targetBtn.classList.add('active');
        currentTool = targetBtn.dataset.tool;
        
        // Reset polygon drawing state if switching tools
        if (currentTool !== 'polygon') {
            resetPolygonState();
        }

        if (currentTool === 'select') {
            canvas.isDrawingMode = false;
            canvas.selection = true;
            canvas.defaultCursor = 'default';
        } else {
            canvas.selection = false;
            canvas.defaultCursor = 'crosshair';
            canvas.discardActiveObject();
            canvas.requestRenderAll();
            hideProperties();
        }
    });
});

function resetPolygonState() {
    polygonPoints = [];
    polygonLines.forEach(l => canvas.remove(l));
    polygonLines = [];
    if(activeLine) {
        canvas.remove(activeLine);
        activeLine = null;
    }
    canvas.requestRenderAll();
}

// Canvas Interactions
function setupEvents() {
    canvas.on('mouse:wheel', function(opt) {
        let delta = opt.e.deltaY;
        let zoom = canvas.getZoom();
        zoom *= 0.999 ** delta;
        if (zoom > 5) zoom = 5;
        if (zoom < 0.1) zoom = 0.1;
        canvas.zoomToPoint({ x: opt.e.offsetX, y: opt.e.offsetY }, zoom);
        opt.e.preventDefault();
        opt.e.stopPropagation();
    });

    canvas.on('mouse:down', function(opt) {
        const evt = opt.e;
        if (evt.altKey === true || evt.code === 'Space') {
            this.isDragging = true;
            this.selection = false;
            this.lastPosX = evt.clientX;
            this.lastPosY = evt.clientY;
            return;
        }

        let pointer = canvas.getPointer(opt.e);

        if (currentTool === 'rect' || currentTool === 'circle') {
            isDrawingShape = true;
            startX = pointer.x;
            startY = pointer.y;

            let defaultProps = {
                left: startX,
                top: startY,
                fill: document.getElementById('sbObjColor').value,
                opacity: 0.8,
                stroke: '#ffffff',
                strokeWidth: 2,
                cornerColor: '#00f3ff',
                cornerStyle: 'circle',
                transparentCorners: false
            };

            if (currentTool === 'rect') {
                activeShape = new fabric.Rect({ ...defaultProps, width: 0, height: 0 });
                activeShape.set('shapeType', 'RECT');
            } else if (currentTool === 'circle') {
                activeShape = new fabric.Ellipse({ ...defaultProps, rx: 0, ry: 0 });
                activeShape.set('shapeType', 'OVAL');
            }
            
            activeShape.set('customLabel', 'Khu vực mới');
            activeShape.set('zoneId', '');
            activeShape.set('uuid', Date.now().toString());
            canvas.add(activeShape);
            
        } else if (currentTool === 'text') {
            let shape = new fabric.IText('Nhập Text', {
                left: pointer.x,
                top: pointer.y,
                fill: '#ffffff',
                strokeWidth: 0,
                fontSize: parseInt(document.getElementById('sbObjFontSize').value) || 20,
                fontFamily: 'Unbounded'
            });
            shape.set('shapeType', 'TEXT');
            shape.set('customLabel', 'Text');
            shape.set('zoneId', '');
            shape.set('uuid', Date.now().toString());
            canvas.add(shape);
            canvas.setActiveObject(shape);
            saveState();
            document.querySelector('.sb-tool[data-tool="select"]').click();
            
        } else if (currentTool === 'seat') {
            let seatColor = document.getElementById('sbObjColor').value;
            let circle = new fabric.Circle({
                radius: 15,
                fill: seatColor,
                stroke: '#ffffff',
                strokeWidth: 2,
                originX: 'center',
                originY: 'center'
            });
            let txt = new fabric.Text('Ghe', {
                originX: 'center',
                originY: 'center',
                fill: '#ffffff',
                fontSize: 10,
                fontFamily: 'Unbounded'
            });
            let group = new fabric.Group([circle, txt], {
                left: pointer.x,
                top: pointer.y,
                originX: 'center',
                originY: 'center',
                cornerColor: '#00f3ff',
                cornerStyle: 'circle',
                transparentCorners: false,
                hasControls: true
            });
            group.set('shapeType', 'SEAT');
            group.set('customLabel', 'Ghe');
            group.set('zoneId', document.getElementById('sbObjTicketType').value || '');
            group.set('uuid', Date.now().toString());
            canvas.add(group);
            canvas.setActiveObject(group);
            saveState();
            
        } else if (currentTool === 'polygon') {
            handlePolygonDown(opt, pointer);
        }
    });

    canvas.on('mouse:move', function(opt) {
        if (this.isDragging) {
            let e = opt.e;
            let vpt = this.viewportTransform;
            vpt[4] += e.clientX - this.lastPosX;
            vpt[5] += e.clientY - this.lastPosY;
            this.requestRenderAll();
            this.lastPosX = e.clientX;
            this.lastPosY = e.clientY;
            return;
        }

        let pointer = canvas.getPointer(opt.e);

        if (isDrawingShape && activeShape) {
            if (activeShape.shapeType === 'RECT') {
                activeShape.set({
                    width: Math.abs(pointer.x - startX),
                    height: Math.abs(pointer.y - startY),
                    left: Math.min(startX, pointer.x),
                    top: Math.min(startY, pointer.y)
                });
            } else if (activeShape.shapeType === 'OVAL') {
                activeShape.set({
                    rx: Math.abs(startX - pointer.x) / 2,
                    ry: Math.abs(startY - pointer.y) / 2,
                    left: Math.min(startX, pointer.x),
                    top: Math.min(startY, pointer.y)
                });
            }
            canvas.renderAll();
        } else if (currentTool === 'polygon' && activeLine) {
            activeLine.set({ x2: pointer.x, y2: pointer.y });
            canvas.renderAll();
        }
    });

    canvas.on('mouse:up', function(opt) {
        this.setViewportTransform(this.viewportTransform);
        this.isDragging = false;

        if (isDrawingShape) {
            isDrawingShape = false;
            if (activeShape) {
                // Ràng buộc kích thước tối thiểu để không tạo hình quá bé
                if (activeShape.shapeType === 'RECT' && activeShape.width < 10) activeShape.set('width', 30);
                if (activeShape.shapeType === 'OVAL' && activeShape.rx < 5) {
                    activeShape.set('rx', 15);
                    activeShape.set('ry', 15);
                }
                
                activeShape.setCoords();
                canvas.setActiveObject(activeShape);
                activeShape = null;
                saveState();
                
                // Tự động quay lại công cụ Chọn sau khi vẽ xong 1 hình
                const selectBtn = document.querySelector('.sb-tool[data-tool="select"]');
                if (selectBtn) selectBtn.click();
            }
        }
    });

    canvas.on('selection:created', showProperties);
    canvas.on('selection:updated', showProperties);
    canvas.on('selection:cleared', hideProperties);
    
    // Ghi nhận thay đổi để lưu Undo
    canvas.on('object:modified', saveState);

    // =============================================
    // SMART GUIDES: Snap & Alignment (nâng cấp)
    // =============================================
    const SNAP_THRESHOLD = 5;      // px để kích hoạt snap căn thẳng (cùng loại)
    const SNAP_THRESHOLD_DIFF = 3;  // px để kích hoạt snap căn thẳng (khác loại)
    const DIST_THRESHOLD = 6;       // px để kích hoạt snap khoảng cách đều (cùng loại)
    let guideLines = [];             // Mảng chứa các đường guide đang hiện trên canvas

    // --- Xóa toàn bộ guide lines ---
    function clearGuides() {
        guideLines.forEach(l => canvas.remove(l));
        guideLines = [];
    }

    // --- Vẽ một đường guide căn thẳng (màu cyan) ---
    function drawAlignGuide(x1, y1, x2, y2) {
        const line = new fabric.Line([x1, y1, x2, y2], {
            stroke: '#00f3ff',
            strokeWidth: 1,
            strokeDashArray: [0], // liền nét để rõ ràng
            selectable: false,
            evented: false,
            opacity: 0.9,
            excludeFromExport: true,
            _isGuide: true
        });
        canvas.add(line);
        canvas.bringToFront(line);
        guideLines.push(line);
    }

    // --- Vẽ đường spacing indicator (màu cam/magenta) ---
    function drawSpacingGuide(x1, y1, x2, y2) {
        const line = new fabric.Line([x1, y1, x2, y2], {
            stroke: '#ff6b9d',
            strokeWidth: 1.5,
            strokeDashArray: [5, 3],
            selectable: false,
            evented: false,
            opacity: 0.85,
            excludeFromExport: true,
            _isGuide: true
        });
        canvas.add(line);
        canvas.bringToFront(line);
        guideLines.push(line);
    }

    // --- Vẽ "tick" đầu/cuối cho spacing indicator ---
    function drawSpacingTick(x, y, isVertical) {
        const TICK = 6;
        const line = new fabric.Line(
            isVertical
                ? [x - TICK, y, x + TICK, y]   // tick ngang cho guide dọc
                : [x, y - TICK, x, y + TICK],  // tick dọc cho guide ngang
            {
                stroke: '#ff6b9d',
                strokeWidth: 1.5,
                selectable: false,
                evented: false,
                opacity: 0.85,
                excludeFromExport: true,
                _isGuide: true
            }
        );
        canvas.add(line);
        canvas.bringToFront(line);
        guideLines.push(line);
    }

    // --- Lấy bounding box và các điểm snap của object ---
    function getSnapPoints(obj) {
        const b = obj.getBoundingRect(true, true);
        return {
            left:    b.left,
            right:   b.left + b.width,
            centerX: b.left + b.width  / 2,
            top:     b.top,
            bottom:  b.top  + b.height,
            centerY: b.top  + b.height / 2,
            width:   b.width,
            height:  b.height
        };
    }

    // --- Nhóm loại object để xét ưu tiên căn chỉnh ---
    // Trả về cùng chuỗi nếu cùng bản chất, khác chuỗi nếu khác bản chất
    function getTypeGroup(obj) {
        const st = obj.shapeType;
        if (st === 'SEAT') return 'SEAT';
        if (st === 'RECT') return 'RECT';
        if (st === 'OVAL') return 'OVAL';
        if (st === 'POLY') return 'POLY';
        if (st === 'TEXT' || obj.type === 'i-text' || obj.type === 'text') return 'TEXT';
        return 'OTHER';
    }

    // Threshold snap dựa trên việc 2 object có cùng loại không
    function snapThresholdFor(moving, other) {
        return getTypeGroup(moving) === getTypeGroup(other)
            ? SNAP_THRESHOLD
            : SNAP_THRESHOLD_DIFF;
    }

    // === Sự kiện di chuyển object ===
    canvas.on('object:moving', function(opt) {
        const moving = opt.target;
        clearGuides();

        // Lọc ra các object khác (bỏ grid, guide, object không selectable)
        const others = canvas.getObjects().filter(o =>
            o !== moving &&
            o.selectable !== false &&
            !o._isGuide &&
            o.evented !== false
        );

        const canvasH = canvas.getHeight() / canvas.getZoom();
        const canvasW = canvas.getWidth()  / canvas.getZoom();

        // ─────────────────────────────────────────────
        // 1. SNAP CĂNG THẲNG (Alignment Snapping)
        //    So sánh 9 cặp điểm L/C/R × L/C/R với từng object khác
        // ─────────────────────────────────────────────
        const m = getSnapPoints(moving);
        let bestSnapX = null; // { diff, guideX }
        let bestSnapY = null; // { diff, guideY }

        // Sắp xếp: cùng loại lên trước để được xét ưu tiên
        const movingGroup = getTypeGroup(moving);
        const sortedOthers = [...others].sort((a, b) => {
            const aSame = getTypeGroup(a) === movingGroup ? 0 : 1;
            const bSame = getTypeGroup(b) === movingGroup ? 0 : 1;
            return aSame - bSame;
        });

        for (const other of sortedOthers) {
            const o = getSnapPoints(other);
            const thr = snapThresholdFor(moving, other);

            // --- Trục X (căn dọc) ---
            const xCandidates = [
                { mVal: m.left,    oVal: o.left    },
                { mVal: m.left,    oVal: o.centerX },
                { mVal: m.left,    oVal: o.right   },
                { mVal: m.centerX, oVal: o.left    },
                { mVal: m.centerX, oVal: o.centerX },
                { mVal: m.centerX, oVal: o.right   },
                { mVal: m.right,   oVal: o.left    },
                { mVal: m.right,   oVal: o.centerX },
                { mVal: m.right,   oVal: o.right   },
            ];
            for (const { mVal, oVal } of xCandidates) {
                const diff = mVal - oVal;
                if (Math.abs(diff) < thr) {
                    // Ưu tiên cùng loại: nếu bestSnapX đang là khác loại thì override dù diff lớn hơn
                    const isSametype = getTypeGroup(other) === movingGroup;
                    const bestIsSame = bestSnapX ? getTypeGroup(bestSnapX._other) === movingGroup : false;
                    if (bestSnapX === null
                        || (!bestIsSame && isSametype)
                        || (isSametype === bestIsSame && Math.abs(diff) < Math.abs(bestSnapX.diff))) {
                        bestSnapX = { diff, guideX: oVal, _other: other };
                    }
                }
            }

            // --- Trục Y (căn ngang) ---
            const yCandidates = [
                { mVal: m.top,     oVal: o.top     },
                { mVal: m.top,     oVal: o.centerY },
                { mVal: m.top,     oVal: o.bottom  },
                { mVal: m.centerY, oVal: o.top     },
                { mVal: m.centerY, oVal: o.centerY },
                { mVal: m.centerY, oVal: o.bottom  },
                { mVal: m.bottom,  oVal: o.top     },
                { mVal: m.bottom,  oVal: o.centerY },
                { mVal: m.bottom,  oVal: o.bottom  },
            ];
            for (const { mVal, oVal } of yCandidates) {
                const diff = mVal - oVal;
                if (Math.abs(diff) < thr) {
                    const isSametype = getTypeGroup(other) === movingGroup;
                    const bestIsSame = bestSnapY ? getTypeGroup(bestSnapY._other) === movingGroup : false;
                    if (bestSnapY === null
                        || (!bestIsSame && isSametype)
                        || (isSametype === bestIsSame && Math.abs(diff) < Math.abs(bestSnapY.diff))) {
                        bestSnapY = { diff, guideY: oVal, _other: other };
                    }
                }
            }
        }

        // Snap vào giữa canvas (nếu gần hơn)
        const midX = canvasW / 2;
        const midY = canvasH / 2;
        for (const mVal of [m.left, m.centerX, m.right]) {
            const diff = mVal - midX;
            if (Math.abs(diff) < SNAP_THRESHOLD) {
                if (bestSnapX === null || Math.abs(diff) < Math.abs(bestSnapX.diff)) {
                    bestSnapX = { diff, guideX: midX, isCanvasCenter: true };
                }
            }
        }
        for (const mVal of [m.top, m.centerY, m.bottom]) {
            const diff = mVal - midY;
            if (Math.abs(diff) < SNAP_THRESHOLD) {
                if (bestSnapY === null || Math.abs(diff) < Math.abs(bestSnapY.diff)) {
                    bestSnapY = { diff, guideY: midY, isCanvasCenter: true };
                }
            }
        }

        // Áp dụng snap X → vẽ guide dọc
        if (bestSnapX !== null) {
            moving.set('left', moving.left - bestSnapX.diff);
            moving.setCoords();
            drawAlignGuide(bestSnapX.guideX, 0, bestSnapX.guideX, canvasH);
        }

        // Áp dụng snap Y → vẽ guide ngang
        if (bestSnapY !== null) {
            moving.set('top', moving.top - bestSnapY.diff);
            moving.setCoords();
            drawAlignGuide(0, bestSnapY.guideY, canvasW, bestSnapY.guideY);
        }

        // ─────────────────────────────────────────────
        // 2. SNAP KHOẢNG CÁCH ĐỀU (Distribution Snapping)
        //    Phát hiện khi moving gần nằm cách đều giữa 2 object khác
        //    hoặc nằm tiếp theo với khoảng cách đều trong chuỗi
        // ─────────────────────────────────────────────
        const ms = getSnapPoints(moving); // bounding sau khi snap căn thẳng

        // --- Phân phối theo trục X (chỉ trong cùng nhóm loại, và chỉ khi chưa có snap căn thẳng trên trục X) ---
        let didDistSnapX = false;
        const sameTypeOthers = others.filter(o => getTypeGroup(o) === movingGroup);
        if (bestSnapX === null && sameTypeOthers.length >= 2) {
            const sortedX = [...sameTypeOthers].sort((a, b) => getSnapPoints(a).left - getSnapPoints(b).left);

            // Kiểm tra xem moving có nên nằm giữa 2 object để tạo khoảng cách đều không
            for (let i = 0; i < sortedX.length - 1; i++) {
                const A = getSnapPoints(sortedX[i]);
                const B = getSnapPoints(sortedX[i + 1]);
                const totalSpan = B.right - A.left;
                const expectedCenterX = A.left + totalSpan / 2;
                if (Math.abs(ms.centerX - expectedCenterX) < DIST_THRESHOLD) {
                    moving.set('left', expectedCenterX - ms.width / 2);
                    moving.setCoords();
                    // Vẽ spacing indicators
                    const tickY = Math.min(A.top, B.top, ms.top) - 14;
                    const ms2 = getSnapPoints(moving);
                    drawSpacingGuide(A.right, tickY, ms2.left, tickY);
                    drawSpacingTick(A.right, tickY, false);
                    drawSpacingTick(ms2.left, tickY, false);
                    drawSpacingGuide(ms2.right, tickY, B.left, tickY);
                    drawSpacingTick(ms2.right, tickY, false);
                    drawSpacingTick(B.left, tickY, false);
                    didDistSnapX = true;
                    break;
                }
            }

            // Kiểm tra nếu moving nằm tiếp theo sau chuỗi (khoảng gap đều)
            if (!didDistSnapX) {
                for (let i = 0; i < sortedX.length - 1; i++) {
                    const A = getSnapPoints(sortedX[i]);
                    const B = getSnapPoints(sortedX[i + 1]);
                    const gap = B.left - A.right;
                    if (gap <= 0) continue;
                    // Nếu moving nằm sau B với khoảng gap tương tự
                    const expectedLeft = B.right + gap;
                    if (Math.abs(ms.left - expectedLeft) < DIST_THRESHOLD) {
                        moving.set('left', expectedLeft);
                        moving.setCoords();
                        const tickY = Math.min(A.top, B.top, ms.top) - 14;
                        const ms2 = getSnapPoints(moving);
                        drawSpacingGuide(A.right, tickY, B.left, tickY);
                        drawSpacingTick(A.right, tickY, false);
                        drawSpacingTick(B.left, tickY, false);
                        drawSpacingGuide(B.right, tickY, ms2.left, tickY);
                        drawSpacingTick(B.right, tickY, false);
                        drawSpacingTick(ms2.left, tickY, false);
                        didDistSnapX = true;
                        break;
                    }
                    // Nếu moving nằm trước A với khoảng gap tương tự
                    const expectedRight = A.left - gap;
                    if (Math.abs(ms.right - expectedRight) < DIST_THRESHOLD) {
                        moving.set('left', expectedRight - ms.width);
                        moving.setCoords();
                        const tickY = Math.min(A.top, B.top, ms.top) - 14;
                        const ms2 = getSnapPoints(moving);
                        drawSpacingGuide(ms2.right, tickY, A.left, tickY);
                        drawSpacingTick(ms2.right, tickY, false);
                        drawSpacingTick(A.left, tickY, false);
                        drawSpacingGuide(A.right, tickY, B.left, tickY);
                        drawSpacingTick(A.right, tickY, false);
                        drawSpacingTick(B.left, tickY, false);
                        didDistSnapX = true;
                        break;
                    }
                }
            }
        }

        // --- Phân phối theo trục Y (chỉ trong cùng nhóm loại, và chỉ khi chưa có snap căn thẳng trên trục Y) ---
        let didDistSnapY = false;
        if (bestSnapY === null && sameTypeOthers.length >= 2) {
            const sortedY = [...sameTypeOthers].sort((a, b) => getSnapPoints(a).top - getSnapPoints(b).top);

            // Kiểm tra xem moving có nên nằm giữa 2 object theo chiều dọc không
            for (let i = 0; i < sortedY.length - 1; i++) {
                const A = getSnapPoints(sortedY[i]);
                const B = getSnapPoints(sortedY[i + 1]);
                const totalSpan = B.bottom - A.top;
                const expectedCenterY = A.top + totalSpan / 2;
                if (Math.abs(ms.centerY - expectedCenterY) < DIST_THRESHOLD) {
                    moving.set('top', expectedCenterY - ms.height / 2);
                    moving.setCoords();
                    const tickX = Math.min(A.left, B.left, ms.left) - 14;
                    const ms2 = getSnapPoints(moving);
                    drawSpacingGuide(tickX, A.bottom, tickX, ms2.top);
                    drawSpacingTick(tickX, A.bottom, true);
                    drawSpacingTick(tickX, ms2.top, true);
                    drawSpacingGuide(tickX, ms2.bottom, tickX, B.top);
                    drawSpacingTick(tickX, ms2.bottom, true);
                    drawSpacingTick(tickX, B.top, true);
                    didDistSnapY = true;
                    break;
                }
            }

            if (!didDistSnapY) {
                for (let i = 0; i < sortedY.length - 1; i++) {
                    const A = getSnapPoints(sortedY[i]);
                    const B = getSnapPoints(sortedY[i + 1]);
                    const gap = B.top - A.bottom;
                    if (gap <= 0) continue;
                    const expectedTop = B.bottom + gap;
                    if (Math.abs(ms.top - expectedTop) < DIST_THRESHOLD) {
                        moving.set('top', expectedTop);
                        moving.setCoords();
                        const tickX = Math.min(A.left, B.left, ms.left) - 14;
                        const ms2 = getSnapPoints(moving);
                        drawSpacingGuide(tickX, A.bottom, tickX, B.top);
                        drawSpacingTick(tickX, A.bottom, true);
                        drawSpacingTick(tickX, B.top, true);
                        drawSpacingGuide(tickX, B.bottom, tickX, ms2.top);
                        drawSpacingTick(tickX, B.bottom, true);
                        drawSpacingTick(tickX, ms2.top, true);
                        didDistSnapY = true;
                        break;
                    }
                    const expectedBottom = A.top - gap;
                    if (Math.abs(ms.bottom - expectedBottom) < DIST_THRESHOLD) {
                        moving.set('top', expectedBottom - ms.height);
                        moving.setCoords();
                        const tickX = Math.min(A.left, B.left, ms.left) - 14;
                        const ms2 = getSnapPoints(moving);
                        drawSpacingGuide(tickX, ms2.bottom, tickX, A.top);
                        drawSpacingTick(tickX, ms2.bottom, true);
                        drawSpacingTick(tickX, A.top, true);
                        drawSpacingGuide(tickX, A.bottom, tickX, B.top);
                        drawSpacingTick(tickX, A.bottom, true);
                        drawSpacingTick(tickX, B.top, true);
                        didDistSnapY = true;
                        break;
                    }
                }
            }
        }

        canvas.requestRenderAll();
    });

    // Xóa guide khi thả chuột hoặc khi hoàn thành thao tác
    canvas.on('object:modified', () => {
        clearGuides();
        saveState();
    });
    canvas.on('mouse:up', () => clearGuides());
}

// --- Polygon Drawing Logic ---
function handlePolygonDown(opt, pointer) {
    if (opt.e.detail === 2 && polygonPoints.length > 2) {
        // Double click: hoàn thành đa giác
        polygonLines.forEach(l => canvas.remove(l));
        if(activeLine) canvas.remove(activeLine);
        
        let poly = new fabric.Polygon(polygonPoints, {
            fill: document.getElementById('sbObjColor').value,
            opacity: 0.8,
            stroke: '#ffffff',
            strokeWidth: 2,
            cornerColor: '#00f3ff',
            cornerStyle: 'circle',
            transparentCorners: false
        });
        poly.set('shapeType', 'POLY');
        poly.set('customLabel', 'Đa giác');
        poly.set('zoneId', '');
        poly.set('uuid', Date.now().toString());
        canvas.add(poly);
        canvas.setActiveObject(poly);

        resetPolygonState();
        saveState();
        document.querySelector('.sb-tool[data-tool="select"]').click();
        return;
    }

    // Add point
    polygonPoints.push({ x: pointer.x, y: pointer.y });
    
    if (polygonPoints.length > 1) {
        let lastPt = polygonPoints[polygonPoints.length - 2];
        let newLine = new fabric.Line([lastPt.x, lastPt.y, pointer.x, pointer.y], {
            strokeWidth: 2,
            stroke: '#00f3ff',
            selectable: false,
            evented: false
        });
        polygonLines.push(newLine);
        canvas.add(newLine);
    }

    if(activeLine) canvas.remove(activeLine);

    activeLine = new fabric.Line([pointer.x, pointer.y, pointer.x, pointer.y], {
        strokeWidth: 2,
        stroke: 'rgba(0, 243, 255, 0.5)',
        strokeDashArray: [5, 5],
        selectable: false,
        evented: false
    });
    canvas.add(activeLine);
}

// --- Properties Panel ---
function showProperties(e) {
    let obj = e.selected[0];
    if (!obj) return;
    
    updateTicketTypeDropdown();
    
    const isText = obj.type === 'i-text' || obj.type === 'text';
    
    document.getElementById('sbObjLabel').disabled = false;
    document.getElementById('sbObjLabel').value = obj.customLabel || (isText ? obj.text : '');
    
    document.getElementById('sbObjTicketType').disabled = isText;
    document.getElementById('sbObjTicketType').value = obj.zoneId || '';
    
    document.getElementById('sbObjColor').disabled = false;
    document.getElementById('sbObjColor').value = obj.fill;
    document.getElementById('sbObjColorHex').textContent = obj.fill;
    
    document.getElementById('sbObjFontSize').disabled = !isText;
    if (isText) document.getElementById('sbObjFontSize').value = obj.fontSize;
    
    document.getElementById('btnFlipH').disabled = false;
    document.getElementById('btnFlipV').disabled = false;
}

function hideProperties() {
    document.getElementById('sbObjLabel').disabled = true;
    document.getElementById('sbObjLabel').value = '';
    
    document.getElementById('sbObjTicketType').disabled = true;
    document.getElementById('sbObjTicketType').value = '';
    
    document.getElementById('sbObjColor').disabled = true;
    document.getElementById('sbObjFontSize').disabled = true;
    
    document.getElementById('btnFlipH').disabled = true;
    document.getElementById('btnFlipV').disabled = true;
}

function sbUpdateLabel(val) {
    let obj = canvas.getActiveObject();
    if (obj) {
        obj.set('customLabel', val);
        if (obj.shapeType === 'SEAT') {
            let txt = obj.getObjects()[1];
            txt.set('text', val);
            // Sắp xếp lại chữ giữa tâm
            obj.addWithUpdate();
        } else if (obj.type === 'i-text' || obj.type === 'text') {
            obj.set('text', val);
        } else {
            // Đối với hình khối, sinh Text nhãn độc lập
            if (!obj.uuid) obj.set('uuid', Date.now().toString());

            let linkedText = null;
            canvas.getObjects().forEach(o => {
                if (o.linkedTextId === obj.uuid) {
                    linkedText = o;
                }
            });

            if (linkedText) {
                linkedText.set('text', val);
                if (val.trim() === '') canvas.remove(linkedText);
            } else if (val.trim() !== '') {
                // Tính toán vị trí trung tâm của hình
                const bounding = obj.getBoundingRect();
                let txt = new fabric.Text(val, {
                    left: bounding.left + bounding.width / 2,
                    top: bounding.top + bounding.height / 2,
                    originX: 'center',
                    originY: 'center',
                    fill: '#ffffff',
                    fontSize: 20,
                    fontFamily: 'Unbounded',
                    linkedTextId: obj.uuid
                });
                txt.set('shapeType', 'TEXT');
                canvas.add(txt);
            }
        }
        canvas.requestRenderAll();
        saveState();
    }
}

function sbUpdateTicketType(val) {
    let obj = canvas.getActiveObject();
    if (obj) {
        obj.set('zoneId', val);
        
        // Tự động cập nhật màu theo khu vực nếu có
        const select = document.getElementById('sbObjTicketType');
        const selectedOption = select.options[select.selectedIndex];
        if (selectedOption && selectedOption.dataset.color) {
            const color = selectedOption.dataset.color;
            sbUpdateColor(color);
            // Cập nhật lại UI color picker
            document.getElementById('sbObjColor').value = color;
            document.getElementById('sbObjColorHex').textContent = color;
        }
        
        saveState();
    }
}

function sbUpdateColor(val) {
    let obj = canvas.getActiveObject();
    if (obj) {
        if (obj.shapeType === 'SEAT') {
            // Đối với ghế (Group), cập nhật màu cho hình tròn bên trong
            let circle = obj.getObjects().find(o => o.type === 'circle');
            if (circle) circle.set('fill', val);
        } else {
            obj.set('fill', val);
        }
        
        document.getElementById('sbObjColorHex').textContent = val;
        canvas.requestRenderAll();
        saveState();
    }
}

function sbUpdateFontSize(val) {
    let obj = canvas.getActiveObject();
    if (obj && (obj.type === 'i-text' || obj.type === 'text')) {
        obj.set('fontSize', parseInt(val));
        canvas.requestRenderAll();
        saveState();
    }
}

function sbDeleteSelected() {
    let activeObjects = canvas.getActiveObjects();
    if (activeObjects.length) {
        canvas.discardActiveObject();
        activeObjects.forEach(function(object) {
            // Xóa cả Text được liên kết nếu đang xóa một Shape
            if (object.uuid) {
                canvas.getObjects().forEach(o => {
                    if (o.linkedTextId === object.uuid) canvas.remove(o);
                });
            }
            canvas.remove(object);
        });
        saveState();
        hideProperties();
    }
}

// --- Shortcuts & Undo/Redo/Copy/Paste ---
function setupKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        if (!document.getElementById('enableStageBuilder').checked) return;
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

        // Delete
        if (e.key === 'Delete' || e.key === 'Backspace') {
            e.preventDefault();
            sbDeleteSelected();
        }
        
        // Ctrl/Cmd + Z
        if ((e.ctrlKey || e.metaKey) && e.key === 'z') {
            e.preventDefault();
            undo();
        }
        
        // Ctrl/Cmd + C
        if ((e.ctrlKey || e.metaKey) && e.key === 'c') {
            e.preventDefault();
            copy();
        }
        
        // Ctrl/Cmd + V
        if ((e.ctrlKey || e.metaKey) && e.key === 'v') {
            e.preventDefault();
            paste();
        }

        // Ctrl/Cmd + D
        if ((e.ctrlKey || e.metaKey) && e.key === 'd') {
            e.preventDefault();
            if (canvas.getActiveObject()) {
                copy();
                setTimeout(paste, 50);
            }
        }
    });
}

function copy() {
    if (!canvas.getActiveObject()) return;
    canvas.getActiveObject().clone(function(cloned) {
        clipboard = cloned;
    });
}

function paste() {
    if (!clipboard) return;
    
    clipboard.clone(function(clonedObj) {
        canvas.discardActiveObject();
        clonedObj.set({
            left: clonedObj.left + 20,
            top: clonedObj.top + 20,
            evented: true,
        });
        
        // Reset uuid for clones so they don't share identical metadata
        if (clonedObj.uuid) clonedObj.set('uuid', Date.now().toString() + Math.floor(Math.random()*1000));
        clonedObj.set('linkedTextId', null); // Don't copy text link blindly
        
        if (clonedObj.type === 'activeSelection') {
            clonedObj.canvas = canvas;
            clonedObj.forEachObject(function(obj) { 
                if (obj.uuid) obj.set('uuid', Date.now().toString() + Math.floor(Math.random()*1000));
                canvas.add(obj); 
            });
            clonedObj.setCoords();
        } else {
            canvas.add(clonedObj);
        }
        clipboard.top += 20;
        clipboard.left += 20;
        canvas.setActiveObject(clonedObj);
        canvas.requestRenderAll();
        saveState();
    });
}

function saveState() {
    if (isRedoing) return;
    
    let json = canvas.toJSON(['shapeType', 'customLabel', 'zoneId', 'uuid', 'linkedTextId']);
    // Filter ra grid lines
    json.objects = json.objects.filter(o => o.selectable !== false || o.type === 'i-text');
    
    const stateStr = JSON.stringify(json);
    
    // Ngăn lưu đè trạng thái giống hệt trạng thái trước đó
    if (undoStack.length === 0 || undoStack[undoStack.length - 1] !== stateStr) {
        undoStack.push(stateStr);
    }
}

function undo() {
    if (undoStack.length > 1) {
        isRedoing = true;
        undoStack.pop(); // Remove state hiện tại
        let prevState = undoStack[undoStack.length - 1]; // Load state liền trước
        
        canvas.loadFromJSON(prevState, function() {
            drawGrid(); // Vẽ lại lưới nằm dưới
            canvas.getObjects().forEach(obj => {
                if(obj.stroke === '#ffffff11') canvas.sendToBack(obj);
            });
            canvas.renderAll();
            
            // Xóa selection
            canvas.discardActiveObject();
            hideProperties();
            
            isRedoing = false;
        });
    }
}

// --- Export Data for Backend ---
function getStageBuilderData() {
    const isEnabled = document.getElementById('enableStageBuilder').checked;
    if (!isEnabled || !canvas) return null;

    let json = canvas.toJSON(['shapeType', 'customLabel', 'zoneId', 'uuid', 'linkedTextId']);
    json.objects = json.objects.filter(o => o.selectable !== false || o.type === 'i-text' || o.type==='text');

    let zones = [];
    let dsGhe = [];
    json.objects.forEach(obj => {
        if (obj.shapeType === 'SEAT') {
            dsGhe.push({
                maKhuVuc: obj.zoneId ? parseInt(obj.zoneId) : null,
                toaDo: obj.customLabel || '',
            });
        } else if (obj.shapeType) {
            let z = {
                tenHienThi: obj.customLabel || '',
                loaiHinhDang: obj.shapeType,
                mauSac: obj.fill,
                kichThuocFont: obj.fontSize || 0,
                maKhuVuc: obj.zoneId ? parseInt(obj.zoneId) : null,
                thuocTinhJson: JSON.stringify(obj)
            };
            zones.push(z);
        }
    });

    return {
        duLieuCanvas: JSON.stringify(json),
        zones: zones,
        dsGhe: dsGhe
    };
}

function sbFlipHorizontal() {
    let obj = canvas.getActiveObject();
    if (obj) {
        obj.set('flipX', !obj.flipX);
        canvas.requestRenderAll();
        saveState();
    }
}

function sbFlipVertical() {
    let obj = canvas.getActiveObject();
    if (obj) {
        obj.set('flipY', !obj.flipY);
        canvas.requestRenderAll();
        saveState();
    }
}
