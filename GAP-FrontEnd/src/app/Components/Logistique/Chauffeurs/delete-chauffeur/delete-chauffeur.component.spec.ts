import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteChauffeurComponent } from './delete-chauffeur.component';

describe('DeleteChauffeurComponent', () => {
  let component: DeleteChauffeurComponent;
  let fixture: ComponentFixture<DeleteChauffeurComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DeleteChauffeurComponent]
    });
    fixture = TestBed.createComponent(DeleteChauffeurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
