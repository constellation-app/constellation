graph [
	node [
		id n0
		Type Person
	]
	node [
		id n1
		Type Document
	]
	node [
		id n2
		Type Country
	]
	node [
		id n2
		Type Country
	]
	edge [
		source n0
		target n1
		Type Correlation
	]
	edge [
		source n2
		target n0
		Type Communication
	]
]