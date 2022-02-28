title_count = 0
abstract_count = 0

# Shouldn't work, but it does
with open('./corpus/cran.all.1400') as f:
    lines = f.readlines()
    join_titles = False
    title = ""
    title_list = []
    join_abstracts = False
    abstract = ""
    abstract_list = []

    for line in lines:
        
        # Titles
        if line.strip() == ".A":
            if join_titles:
                title_list.append(title)
                title = ""
            join_titles = False
            
        if join_titles:
            title += line.replace("\n"," ").replace("\r"," ").replace(" .","")

        if line.strip() == ".T":
            join_titles = True
            title_count += 1

        # Abstracts
        if '.I' in line:
            if join_abstracts:
                abstract_list.append(abstract)
                abstract = ""
            join_abstracts = False
            
        if join_abstracts:
            abstract += line.replace("\n"," ").replace("\r"," ").replace(" .","")

        if line.strip() == ".W":
            join_abstracts = True
            abstract_count += 1

# Reveals that the counts are off. If you put the title count and abstract count in front
# of the default "" reset for title and abstract, you'll find they eventually drift.
# However, the title and abstract match appropriately in almost all cases. Very weird.

# print(f"Title Count: {title_count}")
# print(f"Length Title List: {len(title_list)}\n")

# print(f"Abstract Count: {abstract_count}")
# print(f"Length Abstract List: {len(abstract_list)}")

# print(title_list[-1])
# print(abstract_list[-1])



with open ('corpus_data.txt', 'w') as f:
    for i in range(len(abstract_list)):
        f.write(title_list[i] + '\n')
        f.write(abstract_list[i] + '\n')

    


