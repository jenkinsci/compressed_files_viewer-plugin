const zipButtonsCells = document.getElementsByClassName("topParent")
var zipRowsIndexes = []

for (let i = 0; i < zipButtonsCells.length; i++) {
	zipRowsIndexes[i] = zipButtonsCells[i].parentElement.rowIndex
}

const topNestedElements = document.getElementsByClassName("topNested")
for (let zipButtonsCell of zipButtonsCells) {
	zipButtonsCell.addEventListener("click", function() {
		topNestedElements[zipRowsIndexes.indexOf(this.parentElement.rowIndex)].classList.toggle("show")
		this.firstElementChild.classList.toggle("dropbtn-down")
	})
}

import JsZip from 'jszip';

const extractedFiles = [];

const openAndInsertExtractedFiles = async response => {
	var blob = response;
	var emptyParentZip = new JsZip();
	var numberOfCompressedFiles = 0;
	const parentZip = await emptyParentZip.loadAsync(blob)
	const fileNames = Object.keys(parentZip.files)
	for (let fileName of fileNames) {
		if(isCompressed(fileName)) {
			const data = await parentZip.files[fileName].async('ArrayBuffer')
			var emptyInnerZip = new JsZip();
			const innerZip = await emptyInnerZip.loadAsync(data)
			numberOfCompressedFiles++
			extractedFiles[numberOfCompressedFiles - 1] = []
			const childFileNames = Object.keys(innerZip.files)
			for (let childFileName of childFileNames) {
				// Add file entry to extractedFiles
				const fileBlob = await innerZip.files[childFileName].async('blob')
				if (isFile(childFileName)) {
					const fileAB = await innerZip.files[childFileName].async('ArrayBuffer')
					extractedFiles[numberOfCompressedFiles - 1].push({folder: '', children: [{name: childFileName, size: fileAB.byteLength, blob: fileBlob}]});
				} else if (isFolder(childFileName)) {
					const folderName = childFileName.slice(0, -1);
					const entry = extractedFiles[numberOfCompressedFiles - 1].find(e => e.folder == folderName);
					if (entry == null) {
						extractedFiles[numberOfCompressedFiles - 1].push({folder: folderName, children: []});	
					}
				} else {
					const fileAB = await innerZip.files[childFileName].async('ArrayBuffer')
					const folderName = childFileName.slice(0, childFileName.lastIndexOf('/'));
					const name = childFileName.slice(childFileName.lastIndexOf('/') + 1);
					const entry = extractedFiles[numberOfCompressedFiles - 1].find(e => e.folder == folderName);
					if (entry == null) {
						extractedFiles[numberOfCompressedFiles - 1].push({folder: folderName, children: [{name: name, size: fileAB.byteLength, blob: fileBlob}]});
					} else {
						entry.children.push({name: name, size: fileAB.byteLength, blob: fileBlob});
					}
				}
			}
		}
	}
	
	for (let i = 0; i < extractedFiles.length; i++) {
		for (let j = 0; j < extractedFiles[i].length; j++) {
			const entry = extractedFiles[i][j]
			const table = document.getElementsByClassName('innerTable')[i];
			
			insertExtractedFileRow(table, entry)
			
			if (entry.folder != '') {
				const rowForInnerTable = table.insertRow()
				rowForInnerTable.classList.add('nested');
				const cell = rowForInnerTable.insertCell(0);
				cell.colSpan = "5";
				const innerTable = document.createElement('table');
				innerTable.classList.add('lastInnerTable');
				for (let k = 0; k < entry.children.length; k++) {
					insertExtractedFileInFolderRow(innerTable, entry.children[k], entry.folder)
				}
				cell.appendChild(innerTable)
			}
		}
	}
	var innerFoldersButtonsCells = document.getElementsByClassName("parent");

	var numberOfFoldersPerZipRowIndex = [];
	var innerFoldersIndexes = [];

	for (let i = 0; i < innerFoldersButtonsCells.length; i++) {
		numberOfFoldersPerZipRowIndex[i] = innerFoldersButtonsCells[i].parentElement.parentElement.parentElement.parentElement.parentElement.rowIndex;
		innerFoldersIndexes[i] = innerFoldersButtonsCells[i].parentElement.rowIndex;
	}

	for (let i = 0; i < innerFoldersButtonsCells.length; i++) {
		innerFoldersButtonsCells[i].addEventListener("click", function() {
			const parentRow = this.parentElement.parentElement.parentElement.parentElement.parentElement.rowIndex;
			const innerFoldersContentsRowsIndexes = innerFoldersIndexes.slice(numberOfFoldersPerZipRowIndex.indexOf(parentRow), innerFoldersIndexes.length);
			this.parentElement.parentElement.querySelectorAll(".nested")[innerFoldersContentsRowsIndexes.indexOf(this.parentElement.rowIndex)].classList.toggle("show");
			this.firstElementChild.classList.toggle("dropbtn-down");
		});
	}
	document.getElementById("loadingText").classList.toggle("hide")
}

// const url = "https://"
const url = document.getElementsByClassName('hiddenData')[1].innerText

var xhr = new XMLHttpRequest();
xhr.open('GET', url, true);

xhr.responseType = 'blob';

xhr.onload = function(e) {
	if (this.status == 200) {
		openAndInsertExtractedFiles(this.response)
	}
};

xhr.onerror = function(e) {
	alert("Error " + e.target.status + " occurred while receiving the compressed file.");
};

xhr.send();

function isCompressed(fileName) {
	if (fileName.indexOf(".") == -1) {
		return false;
	} else {
		const commpressionExt = ['zip', 'rar', 'jar', '7zip', 'gzip'];
		const extension = fileName.substring(fileName.indexOf(".") + 1);
		return commpressionExt.includes(extension);
	}
}

function isFile(fileName) {
	return fileName.indexOf('/') == -1;
}

function isFolder(fileName) {
	return fileName.charAt(fileName.length - 1) == '/';
}

function insertExtractedFileRow(table, entry) {
	const row = table.insertRow()

	const td0 = row.insertCell(0);
	td0.classList.add('iconNested')

	const td1 = row.insertCell(1);
	const icon = document.createElement('img')
	if (entry.folder == '') {
		icon.src = document.getElementsByClassName('hiddenData')[0].innerText + "/plugin/compressed_files_viewer/file.png"
	} else {
		icon.src = document.getElementsByClassName('hiddenData')[0].innerText + "/plugin/compressed_files_viewer/folder.png"
	}
	td1.appendChild(icon)
	
	const td2 = row.insertCell(2);
	if (entry.folder == '') {
		td2.innerText = entry.children[0].name
	} else {
		td2.innerText = entry.folder
	}
	
	const td3 = row.insertCell(3);
	td3.classList.add('fileSize')
	if (entry.folder == '') {
		td3.innerText = humanReadableFileSize(entry.children[0].size)
	}
	
	const td4 = row.insertCell(4);
	if (entry.folder == '') {
		td4.classList.add('view');
		const link = document.createElement('a');
		link.href = "#";
		link.innerText = 'view';
		link.onclick = function() {
			window.open(URL.createObjectURL(entry.children[0].blob))
		}
		td4.appendChild(link);
	} else {
		td4.classList.add('parent');
		const btn = document.createElement('button');
		btn.classList.add('dropbtn');
		btn.innerText = 'Open';
		td4.appendChild(btn);
	}
}

function insertExtractedFileInFolderRow(table, file, folderName) {
	const row = table.insertRow()

	const td0 = row.insertCell(0);
	td0.classList.add('iconNested')

	const td1 = row.insertCell(1);
	const icon = document.createElement('img')
	icon.src = document.getElementsByClassName('hiddenData')[0].innerText + "/plugin/compressed_files_viewer/file.png"
	td1.appendChild(icon)
	
	const td2 = row.insertCell(2);
	td2.innerText = file.name
		
	const td3 = row.insertCell(3);
	td3.classList.add('fileSize')
	td3.innerText = humanReadableFileSize(file.size)
	
	const td4 = row.insertCell(4);
	td4.classList.add('view');
	const link = document.createElement('a');
	link.href = "#";
	link.innerText = 'view';
	link.onclick = function() {
		window.open(URL.createObjectURL(file.blob))
	}
	td4.appendChild(link);
}

function humanReadableFileSize(size) {
    var i = size == 0 ? 0 : Math.floor(Math.log(size) / Math.log(1024));
    return (size / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + ['B', 'KB', 'MB', 'GB', 'TB'][i];
};