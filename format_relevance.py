query_ids = [1, 2, 4, 8, 9, 10, 12, 13, 15, 18, 22, 23, 26, 27, 29, 31, 32, 33, 34, 35]
query_rels = []

with open('./corpus/qrels.text') as f:
    lines = f.readlines()
    for query_id in query_ids:
        first_run = True
        query_rel = []
        for line in lines:
            split_line = line.split(' ')
            if int(split_line[0]) == query_id:
                if first_run:
                    query_rel.append(query_id)
                    first_run = False
                query_rel.append(split_line[1])
        query_rels.append(query_rel)

for query_rel in query_rels:
    for query in query_rel:
        print(f"{query}", end=" ")

    print()


                
